package it.unimib.yourwardrobe.ui.main.fragments;

import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.List;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.domain.model.Garment;
import it.unimib.yourwardrobe.ui.main.viewmodel.CreateOutfitViewModel;

@AndroidEntryPoint
public class CreateOutfitFragment extends Fragment {

    private CreateOutfitViewModel viewModel;
    private AutoCompleteTextView seasonTextView;
    private TextInputEditText nameEditText;
    private Button btnSave;
    private ProgressBar pbSave;

    private LinearLayout containerTops, containerAccessories;
    private View btnAddTop, btnAddBottom, btnAddShoes, btnAddAccessory;
    private ImageView imgBottom, imgShoes;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_create_outfit, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(CreateOutfitViewModel.class);
        initViews(view);
        setupListeners();
        observeViewModel();
    }

    private void initViews(View view) {
        seasonTextView = view.findViewById(R.id.season_text_view);
        nameEditText = view.findViewById(R.id.outfit_name_edit_text);
        btnSave = view.findViewById(R.id.btn_save_outfit);
        pbSave = view.findViewById(R.id.pb_save_outfit);
        containerTops = view.findViewById(R.id.container_tops);
        containerAccessories = view.findViewById(R.id.container_accessories);
        btnAddTop = view.findViewById(R.id.btn_add_top);
        btnAddBottom = view.findViewById(R.id.btn_add_bottom);
        btnAddShoes = view.findViewById(R.id.btn_add_shoes);
        btnAddAccessory = view.findViewById(R.id.btn_add_accessory);
        imgBottom = view.findViewById(R.id.img_bottom_slot);
        imgShoes = view.findViewById(R.id.img_shoes_slot);
    }

    private void setupListeners() {
        nameEditText.addTextChangedListener(new TextWatcher() {
            @Override public void afterTextChanged(Editable s) { viewModel.setOutfitName(s.toString()); }
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
        });
        seasonTextView.setOnItemClickListener((parent, view, position, id) ->
                viewModel.setSelectedSeason((String) parent.getItemAtPosition(position)));
        btnAddTop.setOnClickListener(v -> openSelectionDialog(1));
        btnAddBottom.setOnClickListener(v -> openSelectionDialog(2));
        btnAddShoes.setOnClickListener(v -> openSelectionDialog(3));
        btnAddAccessory.setOnClickListener(v -> openSelectionDialog(4));
        btnSave.setOnClickListener(v -> viewModel.saveOutfit());
    }

    // -------------------------------------------------------------------------
    // Snackbar helpers
    // -------------------------------------------------------------------------

    private void styleSnackbar(Snackbar snackbar) {
        View snackView = snackbar.getView();
        android.graphics.drawable.GradientDrawable bg = new android.graphics.drawable.GradientDrawable();
        bg.setColor(Color.parseColor("#E65100"));
        bg.setCornerRadius(50f * getResources().getDisplayMetrics().density);
        snackView.setBackground(bg);
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) snackView.getLayoutParams();
        int margin = (int) (16 * getResources().getDisplayMetrics().density);
        params.setMargins(margin, margin, margin, margin);
        snackView.setLayoutParams(params);
        TextView tv = snackView.findViewById(com.google.android.material.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        tv.setMaxLines(3);
        android.graphics.Typeface tf = androidx.core.content.res.ResourcesCompat
                .getFont(requireContext(), R.font.popstar);
        if (tf != null) tv.setTypeface(tf);
    }

    private void showSnackbar(String message) {
        if (!isAdded()) return;
        Snackbar snackbar = Snackbar.make(requireView(), message, Snackbar.LENGTH_LONG);
        styleSnackbar(snackbar);
        snackbar.show();
    }

    private void showSnackbarOnActivity(String message) {
        if (!isAdded()) return;
        View activityView = requireActivity().findViewById(android.R.id.content);
        Snackbar snackbar = Snackbar.make(activityView, message, Snackbar.LENGTH_LONG);
        styleSnackbar(snackbar);
        snackbar.show();
    }

    // -------------------------------------------------------------------------
    // Observers
    // -------------------------------------------------------------------------

    private void observeViewModel() {
        viewModel.getAllSeasons().observe(getViewLifecycleOwner(), seasons -> {
            ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(),
                    android.R.layout.simple_dropdown_item_1line, seasons);
            seasonTextView.setAdapter(adapter);
        });

        viewModel.getIsSaveEnabled().observe(getViewLifecycleOwner(), btnSave::setEnabled);

        viewModel.getIsLoading().observe(getViewLifecycleOwner(), isLoading -> {
            if (isLoading != null && isLoading) {
                btnSave.setVisibility(View.GONE);
                pbSave.setVisibility(View.VISIBLE);
            } else {
                btnSave.setVisibility(View.VISIBLE);
                pbSave.setVisibility(View.GONE);
            }
        });

        viewModel.getSelectedTops().observe(getViewLifecycleOwner(), this::updateMultiSlotUI_Tops);
        viewModel.getSelectedAccessories().observe(getViewLifecycleOwner(), this::updateMultiSlotUI_Accessories);
        viewModel.getSelectedBottoms().observe(getViewLifecycleOwner(), list -> updateSingleSlotUI(list, imgBottom, btnAddBottom));
        viewModel.getSelectedShoes().observe(getViewLifecycleOwner(), list -> updateSingleSlotUI(list, imgShoes, btnAddShoes));

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && !error.isEmpty()) showSnackbar(error);
        });

        viewModel.getOutfitSavedSuccessfully().observe(getViewLifecycleOwner(), success -> {
            if (success != null && success) {
                showSnackbar("Outfit salvato!");
                Navigation.findNavController(requireView()).navigateUp();
            }
        });

        viewModel.getOfflineSaveScheduled().observe(getViewLifecycleOwner(), offline -> {
            if (offline != null && offline) {
                btnSave.setVisibility(View.GONE);
                pbSave.setVisibility(View.GONE);
                new Handler(Looper.getMainLooper()).post(() -> {
                    if (isAdded()) {
                        Navigation.findNavController(requireView()).popBackStack();
                        showSnackbarOnActivity("Nessuna connessione. L'outfit verrà salvato quando tornerà internet.");
                    }
                });
            }
        });
    }

    // -------------------------------------------------------------------------
    // UI helpers
    // -------------------------------------------------------------------------

    private void openSelectionDialog(int type) {
        SelectClothesDialogFragment.newInstance(type, false, selected -> {
            if (!selected.isEmpty()) {
                if (type == 1) {
                    List<Garment> currentTops = viewModel.getSelectedTops().getValue();
                    if (currentTops != null) for (Garment g : currentTops) viewModel.toggleTopSelection(g);
                } else if (type == 4) {
                    List<Garment> currentAccessories = viewModel.getSelectedAccessories().getValue();
                    if (currentAccessories != null) for (Garment g : currentAccessories) viewModel.toggleAccessorySelection(g);
                }
                for (Garment garment : selected) {
                    if (type == 1) viewModel.toggleTopSelection(garment);
                    else if (type == 2) viewModel.toggleBottomSelection(garment);
                    else if (type == 3) viewModel.toggleShoesSelection(garment);
                    else if (type == 4) viewModel.toggleAccessorySelection(garment);
                }
            }
        }).show(getChildFragmentManager(), "select_garment");
    }

    private void updateMultiSlotUI_Tops(List<Garment> garments) {
        containerTops.removeAllViews();
        if (garments != null && !garments.isEmpty()) {
            for (Garment garment : garments) {
                ShapeableImageView imageView = createGarmentImageView(garment);
                imageView.setOnClickListener(v -> openSelectionDialog(1));
                containerTops.addView(imageView);
            }
        }
        containerTops.addView(btnAddTop);
    }

    private void updateMultiSlotUI_Accessories(List<Garment> garments) {
        containerAccessories.removeAllViews();
        if (garments != null && !garments.isEmpty()) {
            for (Garment garment : garments) {
                ShapeableImageView imageView = createGarmentImageView(garment);
                imageView.setOnClickListener(v -> openSelectionDialog(4));
                containerAccessories.addView(imageView);
            }
        }
        containerAccessories.addView(btnAddAccessory);
    }

    private ShapeableImageView createGarmentImageView(Garment garment) {
        ShapeableImageView imageView = new ShapeableImageView(requireContext());
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                (int) (100 * getResources().getDisplayMetrics().density),
                (int) (100 * getResources().getDisplayMetrics().density)
        );
        params.setMarginEnd((int) (8 * getResources().getDisplayMetrics().density));
        imageView.setLayoutParams(params);
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setShapeAppearanceModel(
                imageView.getShapeAppearanceModel().toBuilder()
                        .setAllCornerSizes(12 * getResources().getDisplayMetrics().density)
                        .build()
        );
        Glide.with(this).load(garment.getImageUrl())
                .placeholder(R.drawable.ic_launcher_background)
                .centerCrop()
                .into(imageView);
        return imageView;
    }

    private void updateSingleSlotUI(List<Garment> list, ImageView img, View btn) {
        if (list != null && !list.isEmpty()) {
            img.setVisibility(View.VISIBLE);
            btn.setVisibility(View.GONE);
            Glide.with(this).load(list.get(0).getImageUrl()).into(img);
            img.setOnClickListener(v -> btn.performClick());
        } else {
            img.setVisibility(View.GONE);
            btn.setVisibility(View.VISIBLE);
        }
    }
}