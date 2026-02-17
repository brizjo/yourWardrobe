package it.unimib.yourwardrobe.ui.main.fragments;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.android.material.textview.MaterialTextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.ClothesAdapter;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.viewmodel.HomeViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;
import it.unimib.yourwardrobe.utils.WeatherUtil;

public class PlannerDialogFragment extends BottomSheetDialogFragment {

    public static final String TAG = PlannerDialogFragment.class.getSimpleName();

    private static final int DAY_COUNT  = 5;
    private static final String ARG_LAT = "lat";
    private static final String ARG_LON = "lon";

    private HomeViewModel homeViewModel;

    // Stato
    private final long[] dayTimestamps    = new long[DAY_COUNT];
    private final int[]  selectedDayIndex = {0};
    private List<Garment> lastPlannedGarments = null;
    private String lastPlannedSeason  = "Primavera";
    private String lastDayLabel       = "";
    private String lastTimeLabel      = "";
    private String lastOccasionLabel  = "";

    // Flag per ignorare valori cached del LiveData
    private boolean isGenerationPending = false;

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    public static PlannerDialogFragment newInstance(double lat, double lon) {
        PlannerDialogFragment sheet = new PlannerDialogFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_LAT, lat);
        args.putDouble(ARG_LON, lon);
        sheet.setArguments(args);
        return sheet;
    }

    // -------------------------------------------------------------------------
    // Lifecycle
    // -------------------------------------------------------------------------

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        homeViewModel = new ViewModelProvider(requireParentFragment())
                .get(HomeViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(
                R.layout.fragment_bottom_sheet_outfit_planner, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setupDaySelector(view);
        setupGenerateButton(view);
        setupBackAndSaveButtons(view);
        observeViewModel(view);
    }

    // -------------------------------------------------------------------------
    // Setup UI — Pagina 1
    // -------------------------------------------------------------------------

    private void setupDaySelector(View view) {
        LinearLayout llDays      = view.findViewById(R.id.ll_days);
        SimpleDateFormat sdfDay  = new SimpleDateFormat("EEE", Locale.ITALIAN);
        SimpleDateFormat sdfDate = new SimpleDateFormat("d MMM", Locale.ITALIAN);

        Calendar cal     = Calendar.getInstance();
        List<Chip> chips = new ArrayList<>();
        selectedDayIndex[0] = 0;

        for (int i = 0; i < DAY_COUNT; i++) {
            dayTimestamps[i] = cal.getTimeInMillis();

            String dayName = i == 0
                    ? "Oggi"
                    : capitalize(sdfDay.format(cal.getTime()));
            String dateStr = sdfDate.format(cal.getTime());

            Chip chip = new Chip(requireContext());
            chip.setText(dayName + "\n" + dateStr);
            chip.setCheckable(true);
            chip.setChecked(i == 0);
            chip.setChipCornerRadius(16f);
            chip.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);

            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 12, 0);
            chip.setLayoutParams(params);

            final int idx = i;
            chip.setOnCheckedChangeListener((btn, checked) -> {
                if (checked) {
                    selectedDayIndex[0] = idx;
                    for (int j = 0; j < chips.size(); j++) {
                        if (j != idx) chips.get(j).setChecked(false);
                    }
                }
            });

            chips.add(chip);
            llDays.addView(chip);
            cal.add(Calendar.DAY_OF_YEAR, 1);
        }
    }

    private void setupGenerateButton(View view) {
        ChipGroup chipGroupTime     = view.findViewById(R.id.chip_group_time);
        ChipGroup chipGroupOccasion = view.findViewById(R.id.chip_group_occasion);
        MaterialButton btnGenerate  = view.findViewById(R.id.btn_generate_planned_outfit);

        Map<Integer, Integer> chipToHour = new HashMap<>();
        chipToHour.put(R.id.chip_morning,   9);
        chipToHour.put(R.id.chip_afternoon, 15);
        chipToHour.put(R.id.chip_evening,   21);

        Map<Integer, String> chipToOccasion = new HashMap<>();
        chipToOccasion.put(R.id.chip_casual,   "Casual");
        chipToOccasion.put(R.id.chip_business, "Business");
        chipToOccasion.put(R.id.chip_elegant,  "Elegant");
        chipToOccasion.put(R.id.chip_sport,    "Sport");

        Map<Integer, String> chipToOccasionLabel = new HashMap<>();
        chipToOccasionLabel.put(R.id.chip_casual,   "casual");
        chipToOccasionLabel.put(R.id.chip_business, "lavoro");
        chipToOccasionLabel.put(R.id.chip_elegant,  "serata");
        chipToOccasionLabel.put(R.id.chip_sport,    "sport");

        Map<Integer, String> chipToTimeLabel = new HashMap<>();
        chipToTimeLabel.put(R.id.chip_morning,   "mattina");
        chipToTimeLabel.put(R.id.chip_afternoon, "pomeriggio");
        chipToTimeLabel.put(R.id.chip_evening,   "sera");

        btnGenerate.setOnClickListener(v -> {
            double lat = getArguments() != null ? getArguments().getDouble(ARG_LAT) : 0;
            double lon = getArguments() != null ? getArguments().getDouble(ARG_LON) : 0;

            if (lat == 0 && lon == 0) {
                Toast.makeText(getContext(),
                        "Posizione non disponibile, riprova tra poco",
                        Toast.LENGTH_SHORT).show();
                return;
            }

            // Imposta le etichette PRIMA di chiamare il ViewModel
            long selectedTs = dayTimestamps[selectedDayIndex[0]];
            int targetHour  = chipToHour.getOrDefault(chipGroupTime.getCheckedChipId(), 12);
            String occasion = chipToOccasion.getOrDefault(
                    chipGroupOccasion.getCheckedChipId(), "Casual");

            SimpleDateFormat sdfFull = new SimpleDateFormat("EEEE d MMMM", Locale.ITALIAN);
            lastDayLabel      = selectedDayIndex[0] == 0
                    ? "oggi"
                    : capitalize(sdfFull.format(new Date(selectedTs)));
            lastTimeLabel     = chipToTimeLabel.getOrDefault(
                    chipGroupTime.getCheckedChipId(), "");
            lastOccasionLabel = chipToOccasionLabel.getOrDefault(
                    chipGroupOccasion.getCheckedChipId(), "");

            lastPlannedGarments  = null;
            isGenerationPending  = true;

            homeViewModel.generatePlannedOutfit(lat, lon, selectedTs, targetHour, occasion);
        });
    }

    // -------------------------------------------------------------------------
    // Setup UI — Pagina 2
    // -------------------------------------------------------------------------

    private void setupBackAndSaveButtons(View view) {
        view.findViewById(R.id.btn_back_to_selection).setOnClickListener(v -> {
            isGenerationPending = false;
            view.findViewById(R.id.page_result).setVisibility(GONE);
            view.findViewById(R.id.page_selection).setVisibility(VISIBLE);
            BottomSheetBehavior<?> behavior = getBehavior();
            if (behavior != null) behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        });

        MaterialButton btnSave   = view.findViewById(R.id.btn_save_planned_outfit);
        TextInputEditText etName = view.findViewById(R.id.et_outfit_name);
        TextInputLayout tilName  = view.findViewById(R.id.til_outfit_name);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null
                    ? etName.getText().toString().trim() : "";
            if (name.isEmpty()) {
                tilName.setError("Inserisci un nome per l'outfit");
                return;
            }
            tilName.setError(null);
            if (lastPlannedGarments == null || lastPlannedGarments.isEmpty()) {
                ToastHelper.show(getContext(), "Genera prima un outfit", false);
                return;
            }
            homeViewModel.savePlannedOutfit(name, lastPlannedGarments, lastPlannedSeason);
        });
    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    private void observeViewModel(View view) {

        homeViewModel.plannedWeather.observe(getViewLifecycleOwner(), result -> {
            if (result == null || !isGenerationPending) return;
            if (result.status == it.unimib.yourwardrobe.core.functional.Result.Status.SUCCESS
                    && result.data != null) {
                ((TextView) view.findViewById(R.id.tv_planned_temp))
                        .setText(result.data.getTemperature());
                ((TextView) view.findViewById(R.id.tv_planned_condition))
                        .setText(result.data.getCondition());

                ImageView ivIcon = view.findViewById(R.id.iv_planned_weather_icon);
                Glide.with(this)
                        .load(result.data.getIconUrl())
                        .placeholder(R.drawable.ic_weather)
                        .into(ivIcon);

                lastPlannedSeason = extractSeason(result.data.getTemperature());
            }
        });

        homeViewModel.plannedOutfit.observe(getViewLifecycleOwner(), garments -> {
            if (!isGenerationPending) return;
            lastPlannedGarments = garments;
            showResultPage(view, garments);
        });

        homeViewModel.isGeneratingPlanned.observe(getViewLifecycleOwner(), isGenerating -> {
            MaterialButton btn = view.findViewById(R.id.btn_generate_planned_outfit);
            btn.setEnabled(!Boolean.TRUE.equals(isGenerating));
            btn.setText(Boolean.TRUE.equals(isGenerating) ? "Generazione..." : "Genera outfit");
        });

        homeViewModel.saveOutfitResult.observe(getViewLifecycleOwner(), result -> {
            if (result == null) return;
            MaterialButton btnSave = view.findViewById(R.id.btn_save_planned_outfit);
            switch (result.status) {
                case LOADING:
                    btnSave.setEnabled(false);
                    btnSave.setText("Salvataggio...");
                    break;
                case SUCCESS:
                    btnSave.setEnabled(true);
                    btnSave.setText("Salva outfit");
                    ToastHelper.show(getContext(), "Outfit salvato!", true);
                    homeViewModel.resetSaveOutfitResult();
                    dismiss();
                    break;
                case ERROR:
                    btnSave.setEnabled(true);
                    btnSave.setText("Salva outfit");
                    ToastHelper.show(getContext(), result.message, false);
                    homeViewModel.resetSaveOutfitResult();
                    break;
            }
        });
    }

    // -------------------------------------------------------------------------
    // Pagina risultato
    // -------------------------------------------------------------------------

    private void showResultPage(View view, List<Garment> garments) {
        view.findViewById(R.id.page_selection).setVisibility(GONE);
        view.findViewById(R.id.page_result).setVisibility(VISIBLE);

        BottomSheetBehavior<?> behavior = getBehavior();
        if (behavior != null) behavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        MaterialTextView tvTitle = view.findViewById(R.id.tv_result_title);
        tvTitle.setText(
                "Ecco l'outfit per " + lastDayLabel +
                        " — " + lastTimeLabel +
                        " — occasione " + lastOccasionLabel
        );

        RecyclerView rvPlanned     = view.findViewById(R.id.rv_planned_outfit);
        LinearLayout llEmpty       = view.findViewById(R.id.ll_planned_empty);
        LinearLayout llSaveSection = view.findViewById(R.id.ll_save_section);

        if (garments != null && !garments.isEmpty()) {
            rvPlanned.setVisibility(VISIBLE);
            llEmpty.setVisibility(GONE);
            llSaveSection.setVisibility(VISIBLE);
            rvPlanned.setLayoutManager(new GridLayoutManager(getContext(), 2));
            rvPlanned.setAdapter(new ClothesAdapter(
                    garments, R.layout.item_outfit_home, (itemView, g) -> {}));
        } else {
            rvPlanned.setVisibility(GONE);
            llEmpty.setVisibility(VISIBLE);
            llSaveSection.setVisibility(GONE);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    @Nullable
    private BottomSheetBehavior<?> getBehavior() {
        if (getDialog() == null) return null;
        View sheet = getDialog().findViewById(
                com.google.android.material.R.id.design_bottom_sheet);
        if (sheet == null) return null;
        return BottomSheetBehavior.from(sheet);
    }

    private String extractSeason(String temperature) {
        try {
            double temp = Double.parseDouble(temperature.replace("°C", "").trim());
            return WeatherUtil.getSeasonFromTemperature(temp);
        } catch (NumberFormatException e) {
            return "Primavera";
        }
    }

    private String capitalize(String s) {
        if (s == null || s.isEmpty()) return s;
        return s.substring(0, 1).toUpperCase() + s.substring(1);
    }
}