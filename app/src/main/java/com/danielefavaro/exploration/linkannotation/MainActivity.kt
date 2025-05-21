package com.danielefavaro.exploration.linkannotation

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.danielefavaro.exploration.linkannotation.ui.theme.MyApplicationTheme

private const val TAG = "LinkBugDemo"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        IssueSample()
                    }
                }
            }
        }
    }
}

@Composable
private fun rememberAnnotatedStringForDemo(isRowLayoutActive: Boolean): AnnotatedString {
    return remember(isRowLayoutActive) { // Re-remember if isRowLayoutActive changes, to update log
        buildAnnotatedString {
            append("By continuing you agree to the EasyPark ")
            pushLink(
                LinkAnnotation.Url(
                    styles = TextLinkStyles(
                        style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                        pressedStyle = SpanStyle(color = Color.Magenta)
                    ),
                    url = "https://example.com/terms",
                    linkInteractionListener = {
                        Log.d(
                            TAG,
                            "BUG_REPORT: 'Terms & Conditions' link clicked! (Row Layout Active: $isRowLayoutActive)"
                        )
                    }
                )
            )
            append("Terms & Conditions")
            pop()
            append(" and ")
            pushLink(
                LinkAnnotation.Url(
                    url = "https://example.com/privacy",
                    styles = TextLinkStyles(
                        style = SpanStyle(color = Color.Blue, textDecoration = TextDecoration.Underline),
                        pressedStyle = SpanStyle(color = Color.Magenta)
                    ),
                    linkInteractionListener = {
                        Log.d(
                            TAG,
                            "BUG_REPORT: 'Privacy Policy' link clicked! (Row Layout Active: $isRowLayoutActive)"
                        )
                    }
                )
            )
            append("Privacy Policy")
            pop()
        }
    }
}

@Composable
private fun IssueDescriptionHeader() {
    Text(
        "Issue Demo: Wrapped Link Clickable Area",
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(bottom = 8.dp)
    )
    Text(
        "The clickable area of the two URLs incorrectly expand " +
                "vertically to the whole text area IF placed in the Row " +
                "with another component beside it. " +
                "It covers adjacent non-linked text on the same visual lines.",
        style = MaterialTheme.typography.bodyMedium,
        modifier = Modifier.padding(bottom = 16.dp)
    )
}

@Composable
private fun LayoutToggleButton(useRowLayout: Boolean, onToggle: () -> Unit) {
    Button(onClick = onToggle, modifier = Modifier.padding(bottom = 16.dp)) {
        Text(if (useRowLayout) "Switch to: Text Only" else "Switch to: Row with Checkbox")
    }
}

@Composable
private fun ProblematicRowLayout(
    annotatedString: AnnotatedString,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onRowClick: () -> Unit
) {
    Text(
        "Current Layout: Row with Checkbox",
        color = Color.Red,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(
                role = Role.Checkbox,
                onClickLabel = "Toggle agreement checkbox",
                onClick = onRowClick
            )
            .semantics(mergeDescendants = true) {}
            .padding(vertical = 8.dp)
            .border(1.dp, Color.Green),
        verticalAlignment = Alignment.Top
    ) {
        Checkbox(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.clearAndSetSemantics {}
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = annotatedString,
            style = MaterialTheme.typography.headlineLarge,
            modifier = Modifier
                .weight(1f)
                .border(1.dp, Color.Red)
        )
    }
}

@Composable
private fun TextOnlyLayout(annotatedString: AnnotatedString) {
    Text(
        "Current Layout: Text Only",
        color = Color.DarkGray,
        style = MaterialTheme.typography.labelMedium,
        modifier = Modifier.padding(bottom = 4.dp)
    )
    Text(
        text = annotatedString,
        style = MaterialTheme.typography.headlineLarge,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.Magenta)
            .padding(vertical = 8.dp)
    )
}

@Composable
private fun ReproductionInstructions() {
    Text(
        "To reproduce the bug:\n1. Ensure 'Row with Checkbox' layout is active.\n" +
                "2. Ensure you get two lines of text (may require specific screen width or device).\n" +
                "3. Tap on the non-linked text that is vertically aligned to the linked text\n\n" +
                "Expected Behavior (for non-linked text tap): Row click (checkbox toggle), no link click log.\n" +
                "Actual Behavior (for non-linked text tap): URL link click is triggered in the logs.",
        style = MaterialTheme.typography.bodySmall,
        modifier = Modifier.padding(top = 16.dp)
    )
}

@Composable
private fun IssueSample() {
    var checked by remember { mutableStateOf(false) }
    var useRowLayout by remember { mutableStateOf(true) }

    val annotatedString = rememberAnnotatedStringForDemo(isRowLayoutActive = useRowLayout)

    // Log the structure of the AnnotatedString to verify its correctness
    LaunchedEffect(annotatedString) { // Re-log if annotatedString instance changes
        Log.d(
            TAG,
            "BUG_REPORT: AnnotatedString Text: '${annotatedString.text}' (Length: ${annotatedString.length})"
        )
        annotatedString.getLinkAnnotations(0, annotatedString.length)
            .forEachIndexed { index, range ->
                val linkText = annotatedString.text.substring(range.start, range.end)
                Log.d(
                    TAG,
                    "BUG_REPORT: LinkAnnotation $index: text='$linkText', range=[${range.start}-${range.end}], item='${range.item}'"
                )
            }
    }

    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxSize()
    ) {
        IssueDescriptionHeader()
        LayoutToggleButton(useRowLayout = useRowLayout, onToggle = { useRowLayout = !useRowLayout })

        if (useRowLayout) {
            ProblematicRowLayout(
                annotatedString = annotatedString,
                checked = checked,
                onCheckedChange = { newCheckedState ->
                    Log.d(TAG, "BUG_REPORT: Checkbox direct click. New state: $newCheckedState")
                    checked = newCheckedState
                },
                onRowClick = {
                    Log.d(
                        TAG,
                        "BUG_REPORT: Row clicked, toggling checkbox. Current: $checked -> ${!checked}"
                    )
                    checked = !checked
                }
            )
        } else {
            TextOnlyLayout(annotatedString = annotatedString)
        }

        ReproductionInstructions()
    }
}

@Preview(
    showBackground = true,
    widthDp = 300,
    name = "Wrapped Link Bug Demo"
)
@Composable
private fun NarrowPreviewWrappedLinkIssue() {
    MyApplicationTheme {
        IssueSample()
    }
}