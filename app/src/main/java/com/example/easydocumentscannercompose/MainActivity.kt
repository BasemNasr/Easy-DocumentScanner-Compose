package com.example.easydocumentscannercompose

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.text.isDigitsOnly
import com.example.easydocumentscannercompose.ui.theme.EasyDocumentScannerComposeTheme
import com.google.mlkit.vision.documentscanner.GmsDocumentScannerOptions
import com.google.mlkit.vision.documentscanner.GmsDocumentScanning
import com.google.mlkit.vision.documentscanner.GmsDocumentScanningResult
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EasyDocumentScannerComposeTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Top
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.document_scanner_illustration),
                            contentDescription = "Logo"
                        )
                        Spacer(modifier = Modifier.height(20.dp))
                        Text(
                            text = "Document scanner MLKit",
                            style = TextStyle(fontSize = 18.sp, color = Color.Black)
                        )
                        DocumentScannerAttributes()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DocumentScannerAttributes() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        var enableGalleryImport by remember { mutableStateOf(true) }
        var pageLimit by remember { mutableIntStateOf(1) }
        val modes = listOf(
            stringResource(R.string.full),
            stringResource(R.string.base), stringResource(R.string.base_with_filter)
        )
        val context = LocalContext.current

        var selectedModeOption by remember { mutableStateOf(modes[0]) }
        var scannerMode by remember { mutableIntStateOf(GmsDocumentScannerOptions.SCANNER_MODE_FULL) }
        val scope = rememberCoroutineScope()
        val snackbarHostState = remember { SnackbarHostState() }

        val scannerLauncher =
            rememberLauncherForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
                if (result.resultCode == ComponentActivity.RESULT_OK) {
                    val gmsResult =
                        GmsDocumentScanningResult.fromActivityResultIntent(result.data) // get the result
                    gmsResult?.pages?.let { pages ->
                        pages.forEach { page ->
                            val imageUri = page.imageUri // do something with the image
                        }
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "You Scanned ${pages.size} pages"
                            )
                        }
                    }
                    gmsResult?.pdf?.let { pdf ->
                        val pdfUri = pdf.uri // do something with the PDF

                        scope.launch {
                            snackbarHostState.showSnackbar(
                                "Your pdf URI is $pdfUri"
                            )
                        }
                    }
                }
            }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.gallery_import_allowed))
            Spacer(modifier = Modifier.width(22.dp))
            Checkbox(
                checked = enableGalleryImport,
                onCheckedChange = { enableGalleryImport = it })
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(text = stringResource(R.string.number_of_pages_scanned))
            OutlinedTextField(
                value = "$pageLimit",
                onValueChange = {
                    if (it.isNotEmpty() && it.isDigitsOnly()) {
                        pageLimit = it.toInt()
                    }
                },
                textStyle = TextStyle(textAlign = TextAlign.Center),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 10.dp),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )
        }

        Row {
            Text(
                modifier = Modifier.padding(top = 22.dp),
                text = stringResource(R.string.scanner_feature_mode)
            )
            Column {
                RadioGroup(modes, selectedModeOption) { newOption ->
                    selectedModeOption = newOption
                    when (selectedModeOption) {
                        context.getString(R.string.full) -> GmsDocumentScannerOptions.SCANNER_MODE_FULL
                        context.getString(R.string.base) -> GmsDocumentScannerOptions.SCANNER_MODE_BASE
                        context.getString(R.string.base_with_filter) -> GmsDocumentScannerOptions.SCANNER_MODE_BASE_WITH_FILTER
                        else -> GmsDocumentScannerOptions.SCANNER_MODE_FULL
                    }
                }
            }

        }

        Button(onClick = {
            val scannerOptions = GmsDocumentScannerOptions.Builder()
                .setGalleryImportAllowed(enableGalleryImport)
                .setPageLimit(pageLimit)
                .setResultFormats(
                    GmsDocumentScannerOptions.RESULT_FORMAT_JPEG,
                    GmsDocumentScannerOptions.RESULT_FORMAT_PDF
                )
                .setScannerMode(scannerMode)
                .build()
            val scanner = GmsDocumentScanning.getClient(scannerOptions)
            scanner.getStartScanIntent(context as Activity)
                .addOnSuccessListener { intentSender ->
                    scannerLauncher.launch(
                        IntentSenderRequest.Builder(intentSender).build()
                    )
                }.addOnFailureListener {
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            it.localizedMessage
                                ?: context.getString(R.string.error_occurred)
                        )
                    }
                }
        }) {
            Text(stringResource(R.string.scan))
        }

    }
}

@Composable
fun RadioGroup(modes: List<String>, selectedOption: String, onOptionSelected: (String) -> Unit) {
    modes.forEach { option ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(8.dp)
        ) {
            RadioButton(
                selected = selectedOption == option,
                onClick = { onOptionSelected(option) }
            )
            Text(text = option)
        }
    }
}


