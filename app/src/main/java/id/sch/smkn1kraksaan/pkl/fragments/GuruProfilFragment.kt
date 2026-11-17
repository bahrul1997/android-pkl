package id.sch.smkn1kraksaan.pkl.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import id.sch.smkn1kraksaan.pkl.activities.LoginActivity
import id.sch.smkn1kraksaan.pkl.databinding.FragmentProfilBinding
import id.sch.smkn1kraksaan.pkl.utils.SessionManager
import com.bumptech.glide.Glide

class GuruProfilFragment : Fragment() {

    private var _binding: FragmentProfilBinding? = null
    private val binding get() = _binding!!
    private lateinit var session: SessionManager

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentProfilBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        session = SessionManager(requireContext())

        // Isi data profil dari session
        binding.tvNamaProfil.text = session.getNama()
        binding.tvNisnKelas.text = "Guru Pembimbing" // Karena guru gak punya NISN/Kelas
        binding.tvRoleBadge.text = "Guru"

        // Load foto
        Glide.with(this)
            .load(session.getFoto())
            .into(binding.ivFotoProfil)

        // Tombol Logout
        binding.menuLogout.setOnClickListener {
            logout()
        }

        // Sembunyikan menu yang gak perlu buat guru
        binding.menuEditProfil.visibility = View.GONE
        binding.menuGantiPassword.visibility = View.GONE
    }

    private fun logout() {
        session.logout()
        val intent = Intent(requireContext(), LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        requireActivity().finish()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}