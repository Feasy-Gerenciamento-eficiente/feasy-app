package com.example.feasy.ui



import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.feasy.databinding.ActivityPatientDetailsBinding
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import android.net.Uri
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream
import java.util.UUID
import com.example.feasy.SupabaseClientProvider
import io.github.jan.supabase.storage.storage
import io.ktor.http.ContentType
import io.github.jan.supabase.postgrest.from
import coil.load
import com.example.feasy.R






private lateinit var selecionarImagemLauncher: ActivityResultLauncher<String>



private var isExpanded = false
private lateinit var usuarioId: String


class PatientDetailsActivity : AppCompatActivity() {


    private lateinit var binding: ActivityPatientDetailsBinding

    override fun onCreate(savedInstanceState: Bundle?) {

        usuarioId = intent.getStringExtra("USUARIO_ID") ?: ""

        super.onCreate(savedInstanceState)
        binding = ActivityPatientDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)


        binding.cvAvatar.setOnClickListener {
            selecionarImagemLauncher.launch("image/*")
        }

        //ADICIONADO PARA IMAGEM
        selecionarImagemLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                uri?.let {
                    binding.imageAvatar.setImageURI(it)
                    fazerUploadDaImagem(it)
                }
            }





        binding.btnExpand.setOnClickListener {
            alternarExpansao()
        }


        carregarDadosDoPaciente()
    }

    private fun alternarExpansao() {
        isExpanded = !isExpanded

        if (isExpanded) {
            // Mostra conteúdo extra
            binding.collapsibleContent.visibility = View.VISIBLE

            // Gira a setinha para cima
            binding.btnExpand.animate()
                .rotation(180f)
                .setDuration(200)
                .start()
        } else {
            // Esconde conteúdo extra
            binding.collapsibleContent.visibility = View.GONE

            // Volta a setinha para baixo
            binding.btnExpand.animate()
                .rotation(0f)
                .setDuration(200)
                .start()
        }
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

        val fotoUrl = intent.getStringExtra("PROFILE_PIC")

        if (!fotoUrl.isNullOrEmpty()) {
            binding.imageAvatar.load(fotoUrl) {
                crossfade(true)
                placeholder(R.color.avatar_mint)
                error(R.color.avatar_mint)
            }
        }


    }


    private fun uriParaByteArray(uri: Uri): ByteArray {
        val inputStream: InputStream? = contentResolver.openInputStream(uri)
        return inputStream?.readBytes() ?: ByteArray(0)
    }

    private fun fazerUploadDaImagem(uri: Uri) {

        if (usuarioId.isEmpty()) return

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val bytes = uriParaByteArray(uri)

                val nomeArquivo = "profile_pics/${usuarioId}_${UUID.randomUUID()}.jpg"

                SupabaseClientProvider.client.storage
                    .from("feasybucket")
                    .upload(
                        path = nomeArquivo,
                        data = bytes
                    ) {
                        upsert = true
                        contentType = ContentType.Image.JPEG
                    }


                // Gera URL pública
                val publicUrl =  SupabaseClientProvider.client.storage
                    .from("feasybucket")
                    .publicUrl(nomeArquivo)

                // Salva no banco
                salvarUrlNoBanco(publicUrl)

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private suspend fun salvarUrlNoBanco(url: String) {
        try {
            SupabaseClientProvider.client
                .from("usuarios")
                .update(
                    mapOf("profile_pic" to url)
                ) {
                    filter {
                        eq("id", usuarioId)
                    }
                }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }




}
