import io.ktor.auth.Principal
import java.io.Serializable

data class User(
    val userId: Long,
    val avatar: String,
    val email: String,
    val displayName: String,
    val passwordHash: String
) : Serializable, Principal