import com.mock.mockpaymentsdk.models.PaymentRequest
import com.mock.mockpaymentsdk.models.PaymentResponse
import com.mock.mockpaymentsdk.network.PaymentApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException

internal open class PaymentRepository(private val api: PaymentApi) {

    open suspend fun processPayment(request: PaymentRequest): Result<PaymentResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.payment(request)
                if (response.isSuccessful) {
                    response.body()?.let {
                        Result.success(it)
                    } ?: Result.failure(Exception("Empty response body"))
                } else {
                    Result.failure(Exception("Error: ${response.errorBody()?.string()}"))
                }
            } catch (e: HttpException) {
                Result.failure(Exception("Network error: ${e.message}"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
}