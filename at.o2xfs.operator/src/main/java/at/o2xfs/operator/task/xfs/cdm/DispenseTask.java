package at.o2xfs.operator.task.xfs.cdm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import at.o2xfs.log.Logger;
import at.o2xfs.log.LoggerFactory;
import at.o2xfs.operator.task.CloseTaskCommand;
import at.o2xfs.operator.task.TaskCommand;
import at.o2xfs.operator.task.xfs.XfsServiceTask;
import at.o2xfs.operator.task.xfs.cdm.DispenseWizard.Status;
import at.o2xfs.xfs.XfsException;
import at.o2xfs.xfs.cdm.NoteErrorReason;
import at.o2xfs.xfs.cdm.v3_00.CashUnit3;
import at.o2xfs.xfs.cdm.v3_00.CashUnitError3;
import at.o2xfs.xfs.cdm.v3_00.CashUnitInfo3;
import at.o2xfs.xfs.cdm.v3_00.CdmCaps3;
import at.o2xfs.xfs.cdm.v3_00.CurrencyExp3;
import at.o2xfs.xfs.cdm.v3_00.Denomination3;
import at.o2xfs.xfs.cdm.v3_00.Dispense3;
import at.o2xfs.xfs.cdm.v3_30.ItemInfoSummary3_30;
import at.o2xfs.xfs.service.cdm.CdmService;
import at.o2xfs.xfs.service.cdm.xfs3.CashUnitInfoCommand;
import at.o2xfs.xfs.service.cdm.xfs3.DenominationEvent;
import at.o2xfs.xfs.service.cdm.xfs3.DispenseCommand;
import at.o2xfs.xfs.service.cdm.xfs3.DispenseListener;
import at.o2xfs.xfs.service.cmd.event.CancelEvent;
import at.o2xfs.xfs.service.cmd.event.ErrorEvent;
import at.o2xfs.xfs.type.RequestId;

public class DispenseTask extends XfsServiceTask<CdmService> implements DispenseListener {

	private static final Logger LOG = LoggerFactory.getLogger(DispenseTask.class);

	private class CancelCommand extends TaskCommand {

		public CancelCommand() {
			super(true);
		}

		@Override
		public void execute() {
			dispenseCommand.cancel();
			setEnabled(false);
		}
	}

	private CdmCaps3 capabilities;

	private CashUnit3[] cashUnits;

	private List<CashUnit3> sortedCashUnits;

	private long[] values = null;

	private DispenseCommand dispenseCommand = null;

	private boolean complete = false;
	private boolean cancel = false;
	private boolean error = false;

	@Override
	protected Class<CdmService> getServiceClass() {
		return CdmService.class;
	}

	@Override
	protected void execute() {
		String method = "execute()";
		try {
			capabilities = service.getCapabilities();
			CashUnitInfo3 cashUnitInfo = new CashUnitInfoCommand(service).call();
			if (LOG.isDebugEnabled()) {
				LOG.debug(method, "cashUnitInfo=" + cashUnitInfo);
			}
			cashUnits = cashUnitInfo.getList();
			values = new long[cashUnits.length];
			sortedCashUnits = new ArrayList<>();
			for (int i = 0; i < cashUnits.length; i++) {
				CashUnit3 each = cashUnits[i];
				switch (each.getType()) {
					case NA:
					case REJECTCASSETTE:
					case RETRACTCASSETTE:
						break;
					default:
						sortedCashUnits.add(each);
						break;
				}
			}
			Collections.sort(sortedCashUnits, new CashUnitComparator());
			if (sortedCashUnits.isEmpty()) {
				setCloseCommand();
			} else {
				DispenseWizard wizard = createWizard();
				if (Status.OK == wizard.open()) {
					for (DispenseWizardPage each : wizard.getPages()) {
						if (each.getValue() != null) {
							values[indexOf(each.getCashUnit())] = each.getValue().longValue();
						}
					}
					synchronized (this) {
						dispenseCommand = createDispenseCommand();
						try {
							dispenseCommand.addCommandListener(this);
							dispenseCommand.execute();
							getCommands().setBackCommand(new CancelCommand());
							while (!(complete || cancel || error)) {
								wait();
							}
						} finally {
							dispenseCommand.removeCommandListener(this);
						}
						if (complete && capabilities.isIntermediateStacker()) {
							taskManager.execute(new PresentTask(service));
						} else {
							getCommands().setBackCommand(new CloseTaskCommand(taskManager));
						}
					}
				} else {
					taskManager.closeTask();
				}
			}
		} catch (XfsException e) {
			LOG.error(method, "Error executing task", e);
			showException(e);
			setCloseCommand();
		} catch (InterruptedException e) {
			LOG.error(method, "Interrupted", e);
		}
	}

	private int indexOf(CashUnit3 cashUnit) {
		for (int i = 0; i < cashUnits.length; i++) {
			if (cashUnit == cashUnits[i]) {
				return i;
			}
		}
		throw new IllegalArgumentException("cashUnit=" + cashUnit + ",cashUnits=" + Arrays.toString(cashUnits));
	}

	private DispenseWizard createWizard() throws XfsException {
		DispenseWizard result = new DispenseWizard(getCommands(), getContent(), capabilities.getMaxDispenseItems());
		for (CashUnit3 cashUnit : sortedCashUnits) {
			CurrencyExp3 currencyExp = service.getCurrencyExponents().stream().filter(e -> Arrays.equals(e.getCurrencyID(), cashUnit.getCurrencyID())).findFirst().get();
			result.addPage(new DispenseWizardPage(cashUnit, currencyExp));
		}
		return result;
	}

	private DispenseCommand createDispenseCommand() {
		return new DispenseCommand(service,
				new Dispense3.Builder().present(!capabilities.isIntermediateStacker()).denomination(new Denomination3.Builder().values(values).build()).build());
	}

	@Override
	public void onInfoAvailable(ItemInfoSummary3_30 itemInfoSummary) {

	}

	@Override
	public void onInputP6() {

	}

	@Override
	public void onCashUnitError(CashUnitError3 cashUnitError) {

	}

	@Override
	public void onDelayedDispense(long delay) {

	}

	@Override
	public void onStartDispense(RequestId requestId) {

	}

	@Override
	public void onPartialDispense(int dispNum) {

	}

	@Override
	public void onSubDispenseOk(Denomination3 denomination) {

	}

	@Override
	public void onIncompleteDispense(Denomination3 denomination) {

	}

	@Override
	public void onNoteError(NoteErrorReason reason) {

	}

	@Override
	public void onCancel(CancelEvent event) {
		synchronized (this) {
			cancel = true;
			notifyAll();
		}
	}

	@Override
	public void onError(ErrorEvent event) {
		showException(event.getException());
		synchronized (this) {
			error = true;
			notifyAll();
		}
	}

	@Override
	public void onComplete(DenominationEvent event) {
		synchronized (this) {
			complete = true;
			notifyAll();
		}
	}
}