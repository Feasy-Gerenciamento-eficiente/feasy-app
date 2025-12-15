package com.example.feasy.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.feasy.PacienteComUsuario
import com.example.feasy.databinding.ItemPatientCardBinding // O nome vem do XML item_patient_card.xml
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import android.content.Intent
import com.example.feasy.ui.PatientDetailsActivity
import coil.load
import com.example.feasy.R
import android.view.View



class PacientAdapter(
    private var lista: List<PacienteComUsuario>,
    private val onEditarPaciente: (PacienteComUsuario) -> Unit,
    private val onAbrirDetalhes: (PacienteComUsuario) -> Unit
) : RecyclerView.Adapter<PacientAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPatientCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPatientCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
        val paciente = lista[position]


        val usuario = item.usuarios // Dados vindos da tabela 'usuarios'

        // 1. Nome do Paciente (ID no XML: textPatientName)
        holder.binding.textPatientName.text = usuario.nome

        // 2. ID (ID no XML: textPatientId)
        // Mostra só os 4 primeiros dígitos do ID para caber no card
        val idCurto = item.usuarioId.take(4).uppercase()
        holder.binding.textPatientId.text = "ID: $idCurto"

        // 3. Diagnóstico (ID no XML: textDiagnosisValue)
        holder.binding.textDiagnosisValue.text = item.diagnostico




        // 4. Iniciais (ID no XML: textInitials)
        // Pega a primeira letra de cada nome (Limitado a 2 letras)
        val iniciais = usuario.nome.split(" ")
            .filter { it.isNotEmpty() }
            .take(2)
            .mapNotNull { it.firstOrNull()?.toString() }
            .joinToString("")
            .uppercase()

        holder.binding.textInitials.text = iniciais

        // 5. Data de Nascimento e Idade (ID no XML: textBirthDate)
        try {
            // O Supabase manda formato YYYY-MM-DD (ex: 1999-03-12)
            val dataNasc = LocalDate.parse(usuario.dataNascimento)
            val hoje = LocalDate.now()

            // Calcula idade
            val idade = Period.between(dataNasc, hoje).years

            // Formata para o padrão Brasileiro (12/03/1999)
            val formatadorBR = DateTimeFormatter.ofPattern("dd/MM/yyyy")
            val dataBonita = dataNasc.format(formatadorBR)

            holder.binding.textBirthDate.text = "$dataBonita • $idade anos"

        } catch (e: Exception) {
            // Se der erro na conversão (data vazia ou formato errado), mostra o texto original
            holder.binding.textBirthDate.text = usuario.dataNascimento
        }


        holder.itemView.setOnClickListener {
            onAbrirDetalhes(item)
        }

        val fotoUrl = item.usuarios.profilePic

        if (!fotoUrl.isNullOrEmpty()) {

            // MOSTRA A FOTO
            holder.binding.imageAvatar.load(fotoUrl) {
                crossfade(true)
                error(R.color.avatar_mint)
            }

            // ESCONDE AS INICIAIS
            holder.binding.textInitials.visibility = View.GONE

            // REMOVE FUNDO AZUL (opcional, mas recomendado)
            holder.binding.imageAvatar.background = null

        } else {

            // SEM FOTO → MOSTRA INICIAIS
            holder.binding.imageAvatar.setImageResource(android.R.color.transparent)

            holder.binding.textInitials.text = iniciais
            holder.binding.textInitials.visibility = View.VISIBLE

            // RESTAURA FUNDO AZUL
            holder.binding.imageAvatar.setBackgroundResource(R.drawable.avatar_background)
        }




        // ADICIONADO ---
        holder.binding.iconEdit.setOnClickListener {
            onEditarPaciente(item)
        }

    }

    override fun getItemCount(): Int = lista.size

    // --- ADICIONE ESSA FUNÇÃO NOVA AQUI NO FINAL: ---
    fun atualizarLista(novaLista: List<PacienteComUsuario>) {
        lista = novaLista
        notifyDataSetChanged() // Avisa a tela que os dados mudaram
    }

    fun getItem(position: Int): PacienteComUsuario {
        return lista[position]
    }

    // Função para remover visualmente da lista (sem recarregar tudo)
    fun removerItem(position: Int) {
        val listaMutavel = lista.toMutableList()
        listaMutavel.removeAt(position)
        lista = listaMutavel
        notifyItemRemoved(position)
    }


}