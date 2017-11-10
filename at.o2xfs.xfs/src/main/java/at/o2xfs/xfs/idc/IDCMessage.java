package at.o2xfs.xfs.idc;

import at.o2xfs.xfs.XfsConstant;

public enum IdcMessage implements XfsConstant {

	/*
	 * @since v3.00
	 */
	EXEE_IDC_INVALIDTRACKDATA(201L),

	/*
	 * @since v3.00
	 */
	EXEE_IDC_MEDIAINSERTED(203L),

	/*
	 * @since v3.00
	 */
	SRVE_IDC_MEDIAREMOVED(204L),

	/*
	 * @since v3.00
	 */
	SRVE_IDC_CARDACTION(205L),

	/*
	 * @since v3.00
	 */
	USRE_IDC_RETAINBINTHRESHOLD(206L),

	/*
	 * @since v3.00
	 */
	EXEE_IDC_INVALIDMEDIA(207L),

	/*
	 * @since v3.00
	 */
	EXEE_IDC_MEDIARETAINED(208L),

	/*
	 * @since v3.00
	 */
	SRVE_IDC_MEDIADETECTED(209L),

	/*
	 * @since v3.10
	 */
	SRVE_IDC_RETAINBININSERTED(210L),

	/*
	 * @since v3.10
	 */
	SRVE_IDC_RETAINBINREMOVED(211L),

	/*
	 * @since v3.10
	 */
	EXEE_IDC_INSERTCARD(212L),

	/*
	 * @since v3.10
	 */
	SRVE_IDC_DEVICEPOSITION(213L),

	/*
	 * @since v3.10
	 */
	SRVE_IDC_POWER_SAVE_CHANGE(214L),

	/*
	 * @since v3.20
	 */
	EXEE_IDC_TRACKDETECTED(215L),

	/*
	 * @since v3.30
	 */
	EXEE_IDC_EMVCLESSREADSTATUS(216L);

	private final long value;

	private IdcMessage(final long value) {
		this.value = value;
	}

	@Override
	public long getValue() {
		return value;
	}
}