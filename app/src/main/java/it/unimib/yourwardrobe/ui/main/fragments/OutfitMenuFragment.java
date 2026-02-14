package it.unimib.yourwardrobe.ui.main.fragments;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import dagger.hilt.android.AndroidEntryPoint;
import it.unimib.yourwardrobe.R;
import it.unimib.yourwardrobe.adapter.OutfitAdapter;
import it.unimib.yourwardrobe.ui.main.viewmodel.OutfitMenuViewModel;
import it.unimib.yourwardrobe.utils.ToastHelper;

@AndroidEntryPoint
public class OutfitMenuFragment extends Fragment {

    private static final String TAG = "OutfitMenuFragment";
    private OutfitMenuViewModel viewModel;

    public OutfitMenuFragment() {}

    public static OutfitMenuFragment newInstance() {
        return new OutfitMenuFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate chiamato");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView chiamato");
        return inflater.inflate(R.layout.fragment_outfit_menu, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated chiamato");

        viewModel = new ViewModelProvider(this).get(OutfitMenuViewModel.class);

        RecyclerView recyclerViewOutfit = view.findViewById(R.id.outfit_recycler_view);

        if (recyclerViewOutfit == null) {
            Log.e(TAG, "❌ RecyclerView è NULL! Controlla gli ID nel layout");
            return;
        }

        Log.d(TAG, "✅ RecyclerView trovata correttamente");

        recyclerViewOutfit.setLayoutManager(new GridLayoutManager(getContext(), 2));
        recyclerViewOutfit.setNestedScrollingEnabled(false);

        Log.d(TAG, "LayoutManager impostato: GridLayoutManager con 2 colonne");

        OutfitAdapter.OnItemClickListener listener = (v, outfit) -> {
            Log.d(TAG, "Click su outfit: " + outfit.getName());
            Bundle bundle = new Bundle();
            bundle.putSerializable("outfit", outfit);
            Navigation.findNavController(v).navigate(
                    R.id.action_outfitFragment_to_singleOutfitFragment, bundle);
        };

        viewModel.getOutfits().observe(getViewLifecycleOwner(), outfits -> {
            Log.d(TAG, "════════════════════════════════════════");
            Log.d(TAG, "Observer chiamato!");
            Log.d(TAG, "Outfits ricevuti: " + (outfits != null ? outfits.size() : "NULL"));

            if (outfits != null) {
                if (outfits.isEmpty()) {
                    Log.w(TAG, "⚠️ Lista vuota - nessun outfit salvato");
                    ToastHelper.show(getContext(), "Nessun outfit salvato. Creane uno!", false);
                } else {
                    Log.d(TAG, "✅ Trovati " + outfits.size() + " outfit:");
                    for (int i = 0; i < outfits.size(); i++) {
                        Log.d(TAG, "  [" + i + "] Nome: " + outfits.get(i).getName() +
                                ", Garments: " + (outfits.get(i).getGarments() != null ?
                                outfits.get(i).getGarments().size() : "null"));
                    }
                }

                // Crea e imposta adapter
                OutfitAdapter adapter = new OutfitAdapter(outfits, R.layout.item_outfit_grid, listener);
                recyclerViewOutfit.setAdapter(adapter);
                Log.d(TAG, "✅ Adapter impostato sulla RecyclerView");
                Log.d(TAG, "   Item count nell'adapter: " + adapter.getItemCount());
            } else {
                Log.e(TAG, "❌ Lista outfits è NULL!");
            }
            Log.d(TAG, "════════════════════════════════════════");
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Log.e(TAG, "❌❌❌ ERRORE dal ViewModel: " + error);
                ToastHelper.show(getContext(), "Errore: " + error, false);
            }
        });

        Button createOutfitButton = view.findViewById(R.id.create_outfit_button);

        if (createOutfitButton == null) {
            Log.e(TAG, "❌ Button è NULL!");
            return;
        }

        createOutfitButton.setOnClickListener(v -> {
            Log.d(TAG, "Click su 'Crea Outfit' button");
            Navigation.findNavController(v).navigate(
                    R.id.action_outfitFragment_to_createOutfitFragment);
        });

        Log.d(TAG, "Setup completato!");
    }
}
