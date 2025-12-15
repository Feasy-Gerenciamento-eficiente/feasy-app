package com.example.feasy.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.feasy.databinding.ActivityPatientDetailsBinding
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class PatientDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPatientDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        carregarDadosDoPaciente()
    }

    private fun carregarDadosDoPaciente() {

        val nome = intent.getStringExtra("NOME") ?: ""
        val email = intent.getStringExtra("EMAIL") ?: ""
        val dataNasc = intent.getStringExtra("DATA_NASC") ?: ""
        val diagnostico = intent.getStringExtra("DIAGNOSTICO") ?: ""
        val responsavel = intent.getStringExtra("RESPONSAVEL") ?: ""

        // Nome do paciente
        binding.tvPatientName.text = nome

        // Diagnóstico
        binding.tvDiagnosis.text = diagnostico

        // Responsável
        binding.tvResponsavel.text = responsavel

        // Data de nascimento (sem cálculo ainda)
        try {
            // DATA_NASC vem como YYYY-MM-DD
            val dataNascimento = LocalDate.parse(dataNasc)

            val hoje = LocalDate.now()
            val idade = Period.between(dataNascimento, hoje).years

            // Formata para padrão brasileiro
            val formatadorBR = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dataFormatada = dataNascimento.format(formatadorBR)

            // 👇 cada informação no seu campo correto
            binding.tvBirthDate.text = dataFormatada
            binding.tvAge.text = "$idade anos"

        } catch (e: Exception) {
            // fallback seguro
            binding.tvBirthDate.text = dataNasc
            binding.tvAge.text = ""
        }


    }


}
