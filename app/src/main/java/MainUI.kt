import androidx.compose.material.BottomSheetScaffoldDefaults
import androidx.compose.material3.BottomSheetScaffold
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.snapshots.SnapshotStateList
import com.marker.locus.Footer
import com.marker.locus.MainScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainUI(activeContacts : SnapshotStateList<String>) {
    BottomSheetScaffold (
        sheetContent = {
            Footer(lst = activeContacts)
        },
        content = {
            MainScreen()
        },
        sheetPeekHeight = BottomSheetScaffoldDefaults.SheetPeekHeight,
    )
}