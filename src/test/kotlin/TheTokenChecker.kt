import org.ethereum.lists.tokens.*
import org.junit.Test
import java.io.File

class TheTokenChecker {

    @Test
    fun shouldPassForValidToken() {
        val file = getFile("valid/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test
    fun shouldPassForValidTokenWithMoreFields() {
        val file = getFile("valid_more_fields/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }


    @Test(expected = InvalidAddress::class)
    fun shouldFailForInvalidAddress() {
        val file = getFile("invalid_address/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }


    @Test(expected = InvalidChecksum::class)
    fun shouldFailForInvalidChecksum() {
        val file = getFile("invalid_erc55/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidDecimals::class)
    fun shouldFailForInvalidDecimals() {
        val file = getFile("invalid_decimals/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidFileName::class)
    fun shouldFailForInvalidFileName() {
        val file = getFile("invalid_filename/yolo.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidWebsite::class)
    fun shouldFailForInvalidWebsite() {
        val file = getFile("invalid_website/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidJSON::class)
    fun shouldFailForInvalidJSON() {
        val file = getFile("invalid_json/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }


    @Test(expected = InvalidJSON::class)
    fun shouldFailForInvalidDeprecation() {
        val file = getFile("invalid_deprecation/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }


    @Test(expected = InvalidDeprecationMigrationType::class)
    fun shouldFailForInvalidDeprecationMigrationType() {
        val file = getFile("invalid_deprecation_migration/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    @Test(expected = InvalidDeprecationTime::class)
    fun shouldFailForInvalidDeprecationTime() {
        val file = getFile("invalid_deprecation_time/0x6475A7FA6Ed2D5180F0e0a07c2d951D12C0EDB91.json")

        checkTokenFile(file)
    }

    private fun getFile(s: String) = File(javaClass.classLoader.getResource("test_tokens/$s").file)

}