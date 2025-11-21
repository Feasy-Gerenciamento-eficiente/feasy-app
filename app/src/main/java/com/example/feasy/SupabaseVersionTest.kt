import io.github.jan.supabase.SupabaseClient

fun testVersion() {
    println(SupabaseClient::class.java.protectionDomain.codeSource.location)
}
