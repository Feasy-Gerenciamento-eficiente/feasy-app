package com.example.feasy

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.feasy.databinding.ActivityPacientsBinding
import com.example.feasy.ui.AddPacienteActivity

class PacientsActivity : AppCompatActivity() {

    // 1. Configurando o Binding para acessar os botões do XML
    private lateinit var binding: ActivityPacientsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 2. "Inflando" a tela (carregando o visual)
        binding = ActivityPacientsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 3. Configurando o clique do botão
        // Certifique-se que no seu XML o id é btnNewPatient
        binding.btnNewPatient.setOnClickListener {
            val intent = Intent(this, AddPacienteActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        // Deixei comentado com "//" para não dar erro agora.
        // Quando você criar a função de listar, é só tirar as barras.

        // buscarPacientes()

        // Dica: Pode colocar um Toast temporário só pra ver que funcionou ao voltar:
        // Toast.makeText(this, "Voltou para a lista!", Toast.LENGTH_SHORT).show()
    }
}