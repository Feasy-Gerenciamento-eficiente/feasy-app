package com.example.feasy

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import android.content.Intent


class LoginActivity : AppCompatActivity() {

    private val emailCorreto = "teste@email.com"
    private val senhaCorreta = "123456"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val emailInput = findViewById<EditText>(R.id.email_input)
        val passwordInput = findViewById<EditText>(R.id.password_input)
        val btnEntrar = findViewById<Button>(R.id.btnEntrar)

        btnEntrar.setOnClickListener {
            val email = emailInput.text.toString().trim()
            val senha = passwordInput.text.toString().trim()

            if (email.isEmpty() || senha.isEmpty()) {
                Toast.makeText(this, "Preencha todos os campos!", Toast.LENGTH_SHORT).show()
                Log.d("LOGIN", "Erro: campos vazios!")
                return@setOnClickListener
            }

            if (email == emailCorreto && senha == senhaCorreta) {
                Toast.makeText(this, "Login realizado!", Toast.LENGTH_SHORT).show()
                Log.d("LOGIN", "Login efetuado com sucesso!")

                val intent = Intent(this, PacientsActivity::class.java)
                startActivity(intent)
                finish() // para impedir que o usuário volte ao login
            } else {
                Toast.makeText(this, "Email ou senha incorretos", Toast.LENGTH_SHORT).show()
                Log.d("LOGIN", "Login inválido: email ou senha incorretos.")
            }
        }
    }
}
