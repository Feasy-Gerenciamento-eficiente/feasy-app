package com.example.feasy

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
// ⚠️ ATENÇÃO: Verifique se este 'R' é o pacote correto do seu app
import com.example.feasy.R

class PacienteAdapter(private val pacientes: List<Paciente>) :
    RecyclerView.Adapter<PacienteAdapter.PacienteViewHolder>() {

    class PacienteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val viewFundoAvatar: View = itemView.findViewById(R.id.viewAvatarBackground)
        val textIniciais: TextView = itemView.findViewById(R.id.textInitials)
        val textNome: TextView = itemView.findViewById(R.id.textPatientName)
        val textId: TextView = itemView.findViewById(R.id.textPatientId)
        // ⚠️ ATENÇÃO: Confirme se os IDs 'textDobValue' e 'textDiagnosisValue'
        // existem no seu layout 'item_patient_card.xml'
        val textDataNasc: TextView = itemView.findViewById(R.id.textDobValue)
        val textDiagnostico: TextView = itemView.findViewById(R.id.textDiagnosisValue)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PacienteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_patient_card, parent, false)
        return PacienteViewHolder(view)
    }

    override fun onBindViewHolder(holder: PacienteViewHolder, position: Int) {
        val paciente = pacientes[position]

        // --- MUDANÇAS AQUI ---
        // Acessamos os dados de dentro do objeto 'usuarios'
        holder.textNome.text = paciente.usuarios.nome
        holder.textDataNasc.text = paciente.usuarios.dataNascimento

        // O resto continua normal
        holder.textDiagnostico.text = paciente.diagnostico
        holder.textId.text = "ID: ${paciente.id}"
        holder.textIniciais.text = paciente.iniciais
        holder.viewFundoAvatar.background.setTint(paciente.corDoAvatarInt)
    }

    override fun getItemCount(): Int {
        return pacientes.size
    }
}