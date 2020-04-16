import kotlinx.coroutines.runBlocking
import org.ethereum.lists.tokens.*
import org.junit.Test
import java.io.File
import org.kethereum.model.ChainId

class TheTokenChecker {

    @Test
    fun shouldPassForValidToken(): Unit = runBlocking {
        val file = getFile("valid/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test
    fun shouldPassForValidTokenWithMoreFields(): Unit = runBlocking {
        val file = getFile("valid_more_fields/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test
    fun shouldPassForValidTokenWithDeprecationMigrationInstructions(): Unit = runBlocking {
        val file = getFile("valid_deprecation_instructions/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }


    @Test
    fun shouldPassForValidTokenWithDeprecationMigrationNewChain(): Unit = runBlocking {
        val file = getFile("valid_deprecation_newchain/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test
    fun shouldPassForValidTokenEip1191(): Unit = runBlocking {
        val file = getFile("valid_eip1191/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file, false, ChainId(30))
    }


    @Test(expected = InvalidAddress::class)
    fun shouldFailForInvalidAddress(): Unit = runBlocking {
        val file = getFile("invalid_address/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }


    @Test(expected = InvalidChecksum::class)
    fun shouldFailForInvalidChecksum(): Unit = runBlocking {
        val file = getFile("invalid_erc55/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidDecimals::class)
    fun shouldFailForInvalidDecimals(): Unit = runBlocking {
        val file = getFile("invalid_decimals/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidFileName::class)
    fun shouldFailForInvalidFileName(): Unit = runBlocking {
        val file = getFile("invalid_filename/yolo.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidWebsite::class)
    fun shouldFailForInvalidWebsite(): Unit = runBlocking {
        val file = getFile("invalid_website/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidJSON::class)
    fun shouldFailForInvalidJSON(): Unit = runBlocking {
        val file = getFile("invalid_json/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidDeprecationMigrationType::class)
    fun shouldFailForInvalidDeprecationMigrationType(): Unit = runBlocking {
        val file = getFile("invalid_deprecation_migration/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = NumberFormatException::class)
    fun shouldFailForInvalidDeprecationNewChain(): Unit = runBlocking {
        val file = getFile("invalid_deprecation_newchain/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidDeprecationTime::class)
    fun shouldFailForInvalidDeprecationTime(): Unit = runBlocking {
        val file = getFile("invalid_deprecation_time/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = Invalid1191Checksum::class)
    fun shouldFailForInvalidChecksumEip1191(): Unit = runBlocking {
        val file = getFile("invalid_eip1191/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file, false, ChainId(30))
    }

    private fun getFile(s: String) = File(javaClass.classLoader.getResource("test_tokens/$s").file)

}