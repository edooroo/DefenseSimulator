package BlueC2Model;

import CEActionModel.DamageAssessment;
import CEActionModel.Detection;
import CEActionModel.DirectEngagement;
import Common.CEInfo;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.BasicAgentModel;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.Message;

public class BlueCompany extends BasicAgentModel {
	
	public static String _IE_LocNoticeIn = "LocNoticeIn";
	public static String _IE_DirectFireIn = "DirectFireIn";
	
	public static String _OE_DirectFireOut = "DirectFireOut";
	public static String _OE_ReportOut = "ReportOut";
	
	protected static String _CS_Normal = "normal";

	public BlueCompany(CEInfo _myInfo) {
		String _name = "BluePlatoon";
		SetModelName(_name);
		
		/*
		 * Add Input and Output port
		 */
		AddInputEvent(_IE_DirectFireIn);
		AddInputEvent(_IE_LocNoticeIn);
		
		AddOutputEvent(_OE_DirectFireOut);
		AddOutputEvent(_OE_ReportOut);
		
		AddCouplingState(_CS_Normal, true);
		
		
		BlueCompanyC2Action bPC2 = new BlueCompanyC2Action(_myInfo);
		
		DamageAssessment dmgAss = new DamageAssessment(_myInfo);
		Detection detAction = new Detection(_myInfo);
		DirectEngagement engment = new DirectEngagement(_myInfo);
		
		bPC2.Activated();
		dmgAss.Activated();
		detAction.Activated();
		engment.Activated();
		
		
		AddCoupling(_CS_Normal, true, this, _IE_LocNoticeIn, detAction, detAction._IE_LocNoticeIn);
		AddCoupling(_CS_Normal, true, this, _IE_DirectFireIn, dmgAss, dmgAss._IE_DirectFireIn);
		
		AddCoupling(_CS_Normal, true, bPC2, bPC2._OE_ReportOut, this, _OE_ReportOut);
		AddCoupling(_CS_Normal, true, engment, engment._OE_DirectFireOut, this, _OE_DirectFireOut);
		
		AddCoupling(_CS_Normal, true, detAction, detAction._OE_ReportOut, bPC2, bPC2._IE_ReportIn);
		AddCoupling(_CS_Normal, true, dmgAss, dmgAss._OE_AssessOut, bPC2, bPC2._IE_ReportIn);
		
		AddCoupling(_CS_Normal, true, bPC2, bPC2._OE_OrderOut, engment, engment._IE_OrderIn);
		
		
	}

	@Override
	public boolean Delta(Message arg0) {
		// TODO Auto-generated method stub
		return true;
	}

}
