package io.github.lee0701.mboard.service

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.charleskorn.kaml.Yaml
import com.charleskorn.kaml.decodeFromStream
import com.charleskorn.kaml.encodeToStream
import io.github.lee0701.mboard.module.table.CodeConvertTable
import java.io.File
import java.io.InputStream
import java.io.OutputStream

class ImportExportActivity: AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        upgradeTablesToEntry2()
    }

    private fun upgradeTablesToEntry2() {
        val fileNames = listOf(
            "table_hangul_2set_ks5002.yaml",
            "table_hangul_2set_old_hangul.yaml",
            "table_hangul_3set_390.yaml",
            "table_hangul_3set_391.yaml",
            "table_hangul_3set_391_strict.yaml",
            "table_latin_colemak.yaml",
            "table_latin_dvorak.yaml",
            "table_symbol_g.yaml",
        )
        fileNames.forEach { name ->
            val id = resources.getIdentifier(name.split('.').first(), "raw", packageName)
            if(id > 0) {
                upgradeTableToEntry2(
                    resources.openRawResource(id),
                    File(filesDir, name).outputStream(),
                )
            }
        }
    }

    private fun upgradeTableToEntry2(input: InputStream, output: OutputStream) {
        val table = Yaml.default.decodeFromStream<CodeConvertTable>(input)
//        val newTable = CodeConvertTable(map2 = table.map.map { (k, v) -> KeyEvent.keyCodeToString(k) to v.convert() }.toMap())
        val newTable = table
        Yaml.default.encodeToStream(newTable, output)
    }

}