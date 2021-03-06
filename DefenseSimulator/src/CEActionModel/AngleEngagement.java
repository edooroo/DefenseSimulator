package CEActionModel;

import java.util.ArrayList;

import C2OrderMessage.MsgFireOrder;
import C2OrderMessage.MsgOrder;
import Common.CEInfo;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.BasicActionModel;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.Message;

public class AngleEngagement extends BasicActionModel {
	
	public static String _IE_OrderIn = "OrderIn";
	public static String _IE_MyInfoIn = "MyInfoIn";
	
	public static String _OE_AngleFireOut = "AngleFireOut";
	
	//private static String _AWS_DETECTED_ENEMY = "DetectedEnemy";
	
	//private static String _AWS_ORDERTYPE = "OrderType";
	//private static String _AWS_ASSESSMENT = "Assessment";
	
	private static String _AWS_Q_AngleOrder = "AngleFireOrder";
	
	
	private static String _CS_MYINFO = "MyInfo";
		
	
	private String _AS_ACTION = "Action";
	private enum _AS{ 
		Stop, Fire
	}
	

	public AngleEngagement(CEInfo _myInfo) {
		String _name= "AngleEngagementAction";
		SetModelName(_name);
		
		AddInputEvent(_IE_OrderIn);
		AddInputEvent(_IE_MyInfoIn);
		AddOutputEvent(_OE_AngleFireOut);
		
		AddAwState(_AWS_Q_AngleOrder, new ArrayList<MsgFireOrder>());
		
		AddConState(_CS_MYINFO, _myInfo, true, STATETYPE_OBJECT);
		
		AddActState(_AS_ACTION, _AS.Stop, true, STATETYPE_CATEGORY);
		
	}

	@Override
	public boolean Act(Message msg) {
		if(this.GetActStateValue(_AS_ACTION) == _AS.Stop){
			return true;
		}else if(this.GetActStateValue(_AS_ACTION) == _AS.Fire){
			// TODO msg generating
			ArrayList<MsgFireOrder> orderList = (ArrayList<MsgFireOrder>)this.GetAWStateValue(_AWS_Q_AngleOrder);
			
			if(orderList.isEmpty()){
				// TODO maybe error
				System.out.println("empty list");
			}else {
				MsgFireOrder _orderFireMsg = orderList.remove(0);
				
				this.UpdateAWStateValue(_AWS_Q_AngleOrder, orderList);
				msg.SetValue(_OE_AngleFireOut, _orderFireMsg);
			}
			
			
			return true;
		}
		
		return false;
	}

	@Override
	public boolean Decide() {
		if(this.GetActStateValue(_AS_ACTION) == _AS.Stop){
			this.UpdateActStateValue(_AS_ACTION, _AS.Fire);
			return true;
		}else if(this.GetActStateValue(_AS_ACTION) == _AS.Fire){
			ArrayList<MsgFireOrder> orderList = (ArrayList<MsgFireOrder>)this.GetAWStateValue(_AWS_Q_AngleOrder);
			if(orderList.isEmpty()){
				this.UpdateActStateValue(_AS_ACTION, _AS.Stop);
			}else {
				this.UpdateActStateValue(_AS_ACTION, _AS.Fire);	
			}
			
			return true;
		}
		return false;
	}

	@Override
	public boolean Perceive(Message msg) {
		if(msg.GetDstEvent() == _IE_OrderIn){
			MsgOrder _orderMsg = (MsgOrder)msg.GetValue();
			MsgFireOrder _fireOrdMsg = (MsgFireOrder)_orderMsg._orderMsg;
			ArrayList<MsgFireOrder> orderList = (ArrayList<MsgFireOrder>)this.GetAWStateValue(_AWS_Q_AngleOrder);
			
			orderList.add(_fireOrdMsg);
			this.UpdateAWStateValue(_AWS_Q_AngleOrder, orderList);
			
			return true;	
		}else if(msg.GetDstEvent() == _IE_MyInfoIn){
			
			CEInfo _myInfo = (CEInfo)msg.GetValue();
			
			this.UpdateConStateValue(_CS_MYINFO, _myInfo);
			Continue();
		}
		return false;
	}

	@Override
	public double TimeAdvance() {
		if(this.GetActStateValue(_AS_ACTION) == _AS.Stop){
			return Double.POSITIVE_INFINITY;
		}else if(this.GetActStateValue(_AS_ACTION) == _AS.Fire){
			return 0;
		}
		return 0;
	}

}
