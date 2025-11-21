package com.example.feasy.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.feasy.PacienteComUsuario
import com.example.feasy.databinding.ItemPatientCardBinding // O nome vem do XML item_patient_card.xml
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter

class PacientesAdapter(
    private var lista: List<PacienteComUsuario>
) : RecyclerView.Adapter<PacientesAdapter.ViewHolder>() {

    inner class ViewHolder(val binding: ItemPatientCardBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemPatientCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = lista[position]
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

        // DICA EXTRA: Configurar cliques nos ícones de Olho e Editar futuramente
        holder.binding.iconEye.setOnClickListener {
            // Lógica para visualizar detalhes...
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