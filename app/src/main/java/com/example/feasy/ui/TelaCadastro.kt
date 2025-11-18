package com.example.feasy // Ajuste o pacote se necessário

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.feasy.data.model.Usuario
import com.example.feasy.data.repository.FisioterapiaRepository
import kotlinx.coroutines.launch

@Composable
fun TelaCadastroFisioterapeuta() {
    // Variáveis de Estado (guardam o que o usuário digita)
    var nome by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var crefito by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope() // Para rodar tarefas em segundo plano
    val context = LocalContext.current // Para mostrar Toast
    val repository = remember { FisioterapiaRepository() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Novo Fisioterapeuta", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        OutlinedTextField(
            value = nome,
            onValueChange = { nome = it },
            label = { Text("Nome Completo") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("E-mail") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = crefito,
            onValueChange = { crefito = it },
            label = { Text("CREFITO") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = {
                if (nome.isBlank() || email.isBlank() || crefito.isBlank()) {
                    Toast.makeText(context, "Preencha tudo!", Toast.LENGTH_SHORT).show()
                } else {
                    loading = true
                    // Lógica de Envio
                    scope.launch {
                        try {
                            val novoUsuario = Usuario(
                                nome = nome,
                                email = email,
                                rg = null,
                                dataNascimento = null,
                                tipoUsuario = "fisioterapeuta"
                            )

                            // Chama o repositório que criamos antes
                            repository.cadastrarFisio(novoUsuario, crefito, null)

                            Toast.makeText(context, "Sucesso! Cadastrado.", Toast.LENGTH_LONG).show()
                            // Limpar campos
                            nome = ""
                            email = ""
                            crefito = ""

                        } catch (e: Exception) {
                            e.printStackTrace()
                            Toast.makeText(context, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                        } finally {
                            loading = false
                        }
                    }
                }
            },
            enabled = !loading,
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            if (loading) CircularProgressIndicator(color = MaterialTheme.colorScheme.onPrimary)
            else Text("CADASTRAR")
        }
    }
}