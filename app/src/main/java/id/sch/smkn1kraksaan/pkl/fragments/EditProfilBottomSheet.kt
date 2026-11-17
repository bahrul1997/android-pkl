package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.sch.smkn1kraksaan.pkl.databinding.BottomSheetEditProfilBinding

class EditProfilBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditProfilBinding? = null
    private val binding get() = _binding!!

    // Data
    private var namaAwal: String = ""
    private var usernameAwal: String = ""
    private var noHpAwal: String = ""

    fun setData(nama: String, username: String, noHp: String?) {
        this.namaAwal = nama
        this.usernameAwal = username
        this.noHpAwal = noHp ?: "-"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetEditProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set data (readonly semua)
        binding.etNama.setText(namaAwal)
        binding.etUsername.setText(usernameAwal)
        binding.etNoHp.setText(noHpAwal)

        // Tombol tutup aja
        binding.btnSimpanProfil.setOnClickListener {
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}