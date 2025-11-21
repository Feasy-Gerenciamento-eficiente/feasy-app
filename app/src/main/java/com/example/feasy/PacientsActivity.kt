package com.example.feasy

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView // <--- NOVO
import androidx.recyclerview.widget.ItemTouchHelper // <--- NOVO
import android.graphics.Canvas // <--- NOVO
import android.graphics.Paint // <--- NOVO
import android.graphics.Color // <--- NOVO
import com.example.feasy.databinding.ActivityPacientsBinding
import com.example.feasy.ui.AddPacienteActivity
import com.example.feasy.ui.PacientesAdapter
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.cancel
import io.github.jan.supabase.auth.auth

class PacientsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPacientsBinding
    private val scope = MainScope()

    // Variável para guardar a lista COMPLETA (Backup)
    private var listaOriginal: List<PacienteComUsuario> = emptyList()

    // Variável para o Adapter
    private lateinit var adapter: PacientesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPacientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerViewPacientes.layoutManager = LinearLayoutManager(this)

        // Inicializa o adapter vazio
        adapter = PacientesAdapter(emptyList())
        binding.recyclerViewPacientes.adapter = adapter

        // --- AQUI COMEÇA O CÓDIGO DO SWIPE (DESLIZAR PARA APAGAR) ---
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
                return false // Não usamos mover, só deslizar
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition

                // Pega o paciente antes de apagar (para saber o ID)
                val pacienteParaApagar = adapter.getItem(position)

                // 1. Remove visualmente na hora
                adapter.removerItem(position)

                // Atualiza o contador visualmente (subtrai 1)
                atualizarContador(listaOriginal.size - 1)

                // 2. Chama o Supabase para apagar do banco
                deletarPacienteNoBanco(pacienteParaApagar.usuarioId)
            }

            // Desenha o fundo vermelho e a lixeira
            override fun onChildDraw(
                c: Canvas, recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder,
                dX: Float, dY: Float, actionState: Int, isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val paint = Paint()

                if (dX < 0) { // Deslizando para esquerda
                    // Fundo Vermelho
                    paint.color = Color.parseColor("#D32F2F")
                    c.drawRect(
                        itemView.right.toFloat() + dX, itemView.top.toFloat(),
                        itemView.right.toFloat(), itemView.bottom.toFloat(), paint
                    )

                    // Ícone da Lixeira
                    val icon = androidx.core.content.ContextCompat.getDrawable(this@PacientsActivity, R.drawable.deleteicon)
                    if (icon != null) {
                        val margin = (itemView.height - icon.intrinsicHeight) / 2
                        val top = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                        val bottom = top + icon.intrinsicHeight
                        val iconRight = itemView.right - margin
                        val iconLeft = iconRight - icon.intrinsicWidth

                        icon.setBounds(iconLeft, top, iconRight, bottom)
                        icon.draw(c)
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
        // Conecta o Swipe na Lista
        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.recyclerViewPacientes)
        // --- FIM DO CÓDIGO DO SWIPE ---


        binding.btnNewPatient.setOnClickListener {
            startActivity(Intent(this, AddPacienteActivity::class.java))
        }

        // --- CONFIGURAÇÃO DA BUSCA ---
        configurarBarraDePesquisa()
    }

    override fun onResume() {
        super.onResume()
        carregarPacientes()
    }

    // --- FUNÇÃO PARA ATUALIZAR O TEXTO ---
    private fun atualizarContador(qtd: Int) {
        binding.textTitle.text = "Pacientes ($qtd)"
    }

    private fun carregarPacientes() {
        scope.launch {
            try {
                val user = SupabaseClientProvider.client.auth.currentUserOrNull()
                val idFisio = user?.id ?: return@launch

                val listaDoBanco = SupabaseClientProvider.client
                    .from("pacientes")
                    .select(columns = Columns.list("*, usuarios(*)")) {
                        filter { eq("fisioterapeuta_id", idFisio) }
                    }.decodeList<PacienteComUsuario>()

                // 1. Salva no Backup (Original)
                listaOriginal = listaDoBanco

                // 2. Atualiza a tela com a lista completa
                adapter.atualizarLista(listaOriginal)

                atualizarContador(listaOriginal.size)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PacientsActivity, "Erro: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun configurarBarraDePesquisa() {
        binding.editSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                filtrarLista(s.toString())
            }
        })
    }

    private fun filtrarLista(textoDigitado: String) {
        if (textoDigitado.isEmpty()) {
            adapter.atualizarLista(listaOriginal)
            atualizarContador(listaOriginal.size)
        } else {
            val listaFiltrada = listaOriginal.filter { paciente ->
                paciente.usuarios.nome.contains(textoDigitado, ignoreCase = true)
            }
            adapter.atualizarLista(listaFiltrada)
            atualizarContador(listaFiltrada.size)
        }
    }

    // --- FUNÇÃO NOVA PARA APAGAR NO SUPABASE ---
    private fun deletarPacienteNoBanco(idUsuarioPaciente: String) {
        scope.launch {
            try {
                SupabaseClientProvider.client
                    .from("pacientes")
                    .delete {
                        filter {
                            eq("usuario_id", idUsuarioPaciente)
                        }
                    }
                Toast.makeText(this@PacientsActivity, "Paciente removido!", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@PacientsActivity, "Erro ao deletar: ${e.message}", Toast.LENGTH_LONG).show()
                // Se der erro, recarrega a lista para o item voltar
                carregarPacientes()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        scope.cancel()
    }
}