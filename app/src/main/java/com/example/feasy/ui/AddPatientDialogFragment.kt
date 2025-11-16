package com.example.feasy.ui

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.feasy.R
import com.example.feasy.databinding.DialogAddPatientBinding
import com.google.android.material.chip.Chip
import kotlinx.coroutines.launch

// ✔️ IMPORTS CORRETOS DO SUPABASE 2.5.1
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.decodeAs

class AddPatientDialogFragment : DialogFragment() {

    private var _binding: DialogAddPatientBinding? = null
    private val binding get() = _binding!!

    interface OnPatientAddedListener {
        fun onPatientAdded()
    }
    private var listener: OnPatientAddedListener? = null

    fun setOnPatientAddedListener(listener: OnPatientAddedListener) {
        this.listener = listener
    }

    // ✔️ SUPABASE CLIENTE 100% CORRETO
    private val supabase by lazy {
        createSupabaseClient(
            supabaseUrl = "https://ylffveawkvrymekbvbym.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlsZmZ2ZWF3a3ZyeW1la2J2YnltIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyNjQ0NjMsImV4cCI6MjA3ODg0MDQ2M30.iUOSXOUgUPOlEBk3010CAhdNcKeQrkHh50R6tVMySgk"
        ) {
            install(Auth)
            install(Postgrest)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAddPatientBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        isCancelable = false

        binding.buttonClose.setOnClickListener { dismiss() }
        binding.buttonCancelar.setOnClickListener { dismiss() }

        // Ajuste da borda das chips
        val corDaBorda = ContextCompat.getColor(requireContext(), R.color.black)
        binding.chipGroupColor.setOnCheckedStateChangeListener { group, checkedIds ->
            for (i in 0 until group.childCount) {
                val chip = group.getChildAt(i) as Chip
                if (chip.id == checkedIds.firstOrNull()) {
                    chip.chipStrokeWidth = 3f
                    chip.chipStrokeColor = ColorStateList.valueOf(corDaBorda)
                } else {
                    chip.chipStrokeWidth = 0f
                }
            }
        }

        binding.btnNewPatient.setOnClickListener {
            val nome = binding.editTextNome.text.toString()
            val dataNasc = binding.editTextDataNascimento.text.toString()
            val diagnostico = binding.editTextDiagnostico.text.toString()

            val selectedColorId = binding.chipGroupColor.checkedChipId
            val corAvatarHex = getHexColorFromChipId(selectedColorId)

            if (nome.isBlank() || dataNasc.isBlank() || diagnostico.isBlank()) {
                Toast.makeText(requireContext(), "Preencha todos os campos.", Toast.LENGTH_SHORT).show()
            } else if (corAvatarHex == null) {
                Toast.makeText(requireContext(), "Por favor, selecione uma cor.", Toast.LENGTH_SHORT).show()
            } else {
                it.isEnabled = false
                adicionarPacienteAoBanco(nome, dataNasc, diagnostico, corAvatarHex)
            }
        }
    }

    private fun getHexColorFromChipId(chipId: Int): String? {
        return when (chipId) {
            R.id.chip_color_pink -> "#F4C2D8"
            R.id.chip_color_cyan -> "#B2EBF2"
            R.id.chip_color_green -> "#C8E6C9"
            R.id.chip_color_peach -> "#FFDAB9"
            R.id.chip_color_lavender -> "#D1C4E9"
            R.id.chip_color_mint -> "#C8F7C5"
            else -> null
        }
    }

    private fun adicionarPacienteAoBanco(
        nome: String,
        dataNasc: String,
        diagnostico: String,
        corAvatar: String
    ) {
        lifecycleScope.launch {
            try {
                // ✔️ 1 — Inserir usuário
                val dadosUsuario = mapOf(
                    "nome" to nome,
                    "data_nascimento" to dataNasc,
                    "tipo_usuario" to "paciente"
                )

                val novoUsuario = supabase
                    .from("usuarios")
                    .insert(dadosUsuario)
                    .select("id")
                    .decodeAs<Map<String, Int>>()   // <-- API nova

                val novoUsuarioId = novoUsuario["id"]
                    ?: throw Exception("Não foi possível obter o ID do novo usuário.")

                // ✔️ 2 — Inserir paciente
                val idFisioterapeutaLogado = 1 // substitua pela lógica real

                val dadosPaciente = mapOf(
                    "usuario_id" to novoUsuarioId,
                    "fisioterapeuta_id" to idFisioterapeutaLogado,
                    "diagnostico_inicial" to diagnostico,
                    "cor_avatar" to corAvatar
                )

                supabase.from("pacientes").insert(dadosPaciente)

                Toast.makeText(requireContext(), "$nome adicionado!", Toast.LENGTH_SHORT).show()
                listener?.onPatientAdded()
                dismiss()

            } catch (e: Exception) {
                Toast.makeText(requireContext(), "Erro ao salvar: ${e.message}", Toast.LENGTH_LONG).show()
                binding.btnNewPatient.isEnabled = true
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
