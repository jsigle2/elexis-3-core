package ch.elexis.core.ui.medication.handlers;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.ui.medication.views.MedicationTableViewerItem;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;
import ch.elexis.data.Patient;

public class DispenseHandler extends AbstractHandler {
	
	@SuppressWarnings("unchecked")
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		Patient patient = ElexisEventDispatcher.getSelectedPatient();
		if (patient == null) {
			return null;
		}
		
		List<IPrescription> prescRecipes = new ArrayList<IPrescription>();
		
		ISelection selection =
			HandlerUtil.getActiveWorkbenchWindow(event).getActivePage().getSelection();
		if (!selection.isEmpty()) {
			IStructuredSelection strucSelection = (IStructuredSelection) selection;
			List<MedicationTableViewerItem> mtvItems = strucSelection.toList();
			for (MedicationTableViewerItem mtvItem : mtvItems) {
				IPrescription p = mtvItem.getPrescription();
				if (p != null) {
					prescRecipes.add(p);
				}
			}
		}
		for (IPrescription prescription : prescRecipes) {
			CreatePrescriptionHelper prescriptionHelper =
				new CreatePrescriptionHelper(null, HandlerUtil.getActiveShell(event));
			prescriptionHelper.selfDispense(prescription);
		}
		return null;
	}
	
}
