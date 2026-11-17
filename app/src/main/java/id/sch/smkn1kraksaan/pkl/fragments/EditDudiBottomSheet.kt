package id.sch.smkn1kraksaan.pkl.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import id.sch.smkn1kraksaan.pkl.api.ApiClient
import id.sch.smkn1kraksaan.pkl.databinding.BottomSheetEditDudiBinding
import id.sch.smkn1kraksaan.pkl.models.UpdateDudiRequest
import kotlinx.coroutines.launch

class EditDudiBottomSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetEditDudiBinding? = null
    private val binding get() = _binding!!

    private var pimpinanAwal = ""
    private var instrukturAwal = ""
    private var jabatanAwal = ""
    private var nohpAwal = ""

    private var onSuccess: (() -> Unit)? = null

    fun setData(pimpinan: String?, instruktur: String?, jabatan: String?, nohp: String?) {
        this.pimpinanAwal = pimpinan ?: ""
        this.instrukturAwal = instruktur ?: ""
        this.jabatanAwal = jabatan ?: ""
        this.nohpAwal = nohp ?: ""
    }

    fun setOnSuccessListener(listener: () -> Unit) {
        onSuccess = listener
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = BottomSheetEditDudiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.etPimpinan.setText(pimpinanAwal)
        binding.etInstruktur.setText(instrukturAwal)
        binding.etJabatan.setText(jabatanAwal)
        binding.etNohp.setText(nohpAwal)

        binding.btnSimpan.setOnClickListener { submitData() }
    }

    private fun submitData() {
        val pimpinan = binding.etPimpinan.text.toString().trim()
        val instruktur = binding.etInstruktur.text.toString().trim()
        val jabatan = binding.etJabatan.text.toString().trim()
        val nohp = binding.etNohp.text.toString().trim()

        if (instruktur.isEmpty()) {
            Toast.makeText(requireContext(), "Nama Instruktur wajib diisi", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            try {
                val request = UpdateDudiRequest(
                    namaPimpinan = if (pimpinan.isNotEmpty()) pimpinan else null,
                    namaInstruktur = instruktur,
                    jabatanInstruktur = if (jabatan.isNotEmpty()) jabatan else null,
                    nohpInstruktur = if (nohp.isNotEmpty()) nohp else null
                )

                val response = ApiClient.getApiService().simpanDudiInstruktur(request)

                if (response.isSuccessful && response.body()?.status == true) {
                    Toast.makeText(requireContext(), "✅ Berhasil disimpan", Toast.LENGTH_SHORT).show()
                    onSuccess?.invoke()
                    dismiss()
                } else {
                    Toast.makeText(requireContext(), "❌ ${response.body()?.message}", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                Toast.makeText(requireContext(), "❌ Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}