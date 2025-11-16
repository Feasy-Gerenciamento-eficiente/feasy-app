package com.example.feasy

import android.content.Context
import android.os.Bundle
import android.view.MotionEvent
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.AppCompatEditText
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope // Importante para Coroutines
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feasy.ui.AddPatientDialogFragment // Importe seu Dialog
import io.supabase.common.SupabaseOptions
import io.supabase.createSupabaseClient
import io.supabase.gotrue.GoTrue
import io.supabase.postgrest.Postgrest
import io.supabase.postgrest.from
import kotlinx.coroutines.launch

// 1. Implemente a interface do Dialog
class PacientesActivity : AppCompatActivity(), AddPatientDialogFragment.OnPatientAddedListener {

    private val listaDePacientes = mutableListOf<Paciente>()
    private lateinit var pacienteAdapter: PacienteAdapter

    // 2. Crie o cliente Supabase
    private val supabase by lazy {
        createSupabaseClient(
            // ⚠️ AÇÃO: COLOQUE SUAS CREDENCIAIS AQUI
            supabaseUrl = "https://ylffveawkvrymekbvbym.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InlsZmZ2ZWF3a3ZyeW1la2J2YnltIiwicm9sZSI6ImFub24iLCJpYXQiOjE3NjMyNjQ0NjMsImV4cCI6MjA3ODg0MDQ2M30.iUOSXOUgUPOlEBk3010CAhdNcKeQrkHh50R6tVMySgk"
        ) {
            install(GoTrue)
            install(Postgrest)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pacients)

        // --- 1. ENCONTRAR TODOS OS COMPONENTES ---
        val recyclerView: RecyclerView = findViewById(R.id.recyclerViewPacientes)
        val botaoNovoPaciente: Button = findViewById(R.id.btnNewPatient)
        val editSearch = findViewById<AppCompatEditText>(R.id.editSearch)
        val rootLayout = findViewById<ConstraintLayout>(R.id.rootLayout)
        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

        // --- 2. CONFIGURAR O RECYCLERVIEW ---
        // O Adapter é criado com a lista VAZIA
        pacienteAdapter = PacienteAdapter(listaDePacientes)
        recyclerView.adapter = pacienteAdapter
        recyclerView.layoutManager = LinearLayoutManager(this)

        // --- 3. CONFIGURAR O CLIQUE DO BOTÃO (LÓGICA CORRIGIDA) ---
        botaoNovoPaciente.setOnClickListener {
            // A lógica aleatória foi REMOVIDA
            // O botão agora APENAS abre o modal
            mostrarDialogNovoPaciente()
        }

        // --- 4. CARREGAR DADOS DO BANCO ---
        // Busca os dados assim que a tela é criada
        carregarPacientesDoBanco()

        // --- 5. CONFIGURAR AÇÃO DE PESQUISA (Sem mudança) ---
        editSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                realizarPesquisa(v.text.toString())
                imm.hideSoftInputFromWindow(v.windowToken, 0)
                v.clearFocus()
                true
            } else {
                false
            }
        }

        // --- 6. CÓDIGOS DE FOCO (Sem mudança) ---
        rootLayout.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (editSearch.isFocused) {
                    editSearch.clearFocus()
                    imm.hideSoftInputFromWindow(editSearch.windowToken, 0)
                }
            }
            false
        }
        recyclerView.setOnTouchListener { v, event ->
            if (event.action == MotionEvent.ACTION_DOWN) {
                if (editSearch.isFocused) {
                    editSearch.clearFocus()
                    imm.hideSoftInputFromWindow(editSearch.windowToken, 0)
                }
            }
            false
        }
    }

    // --- 7. FUNÇÃO PARA ABRIR O MODAL ---
    private fun mostrarDialogNovoPaciente() {
        val dialog = AddPatientDialogFragment()
        // Diz ao dialog: "Quando você terminar, avise a 'this' (esta Activity)"
        dialog.setOnPatientAddedListener(this)
        dialog.show(supportFragmentManager, "AddPatientDialog")
    }

    // --- 8. FUNÇÃO PARA CARREGAR OS DADOS (LÓGICA NOVA) ---
    private fun carregarPacientesDoBanco() {
        lifecycleScope.launch {
            try {
                // A consulta com JOIN
                val response = supabase.from("pacientes")
                    .select(
                        """
                        id,
                        diagnostico_inicial,
                        cor_avatar, 
                        usuarios!inner(nome, data_nascimento)
                        """.trimIndent()
                    )

                // Decodifica usando o novo 'molde' Paciente
                val pacientesDoBanco = response.decodeList<Paciente>()

                // Atualiza a UI na thread principal
                runOnUiThread {
                    listaDePacientes.clear()
                    listaDePacientes.addAll(pacientesDoBanco)
                    pacienteAdapter.notifyDataSetChanged() // Avisa o adapter
                }

            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(this@PacientesActivity, "Erro ao carregar: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // --- 9. FUNÇÃO DA INTERFACE (LÓGICA NOVA) ---
    // Esta função é chamada pelo Dialog quando um paciente é salvo
    override fun onPatientAdded() {
        Toast.makeText(this, "Atualizando lista...", Toast.LENGTH_SHORT).show()
        // Apenas recarregamos os dados do banco
        carregarPacientesDoBanco()
    }

    // --- 10. FUNÇÃO DE PESQUISA (Sem mudança) ---
    private fun realizarPesquisa(query: String) {
        // TODO: Adicionar a lógica de filtro do adapter aqui
        Toast.makeText(this, "Pesquisando por: $query", Toast.LENGTH_SHORT).show()
    }
}