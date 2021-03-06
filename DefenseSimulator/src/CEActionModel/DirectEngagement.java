package CEActionModel;

import C2OrderMessage.MsgDirEngOrder;
import C2OrderMessage.MsgOrder;
import Common.CEInfo;
import Common.DmgState;
import Common.OrderType;
import Common.UUID;
import Common.WTType;
import Message.MsgDirectFire;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.BasicActionModel;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.Message;

public class DirectEngagement extends BasicActionModel {
	
	public UUID _id;
	
	public static String _IE_OrderIn = "OrderIn";
	public static String _IE_MyInfoIn = "MyInfoIn";
	
	public static String _OE_DirectFireOut = "DirectFireOut";
	
	private static String _AWS_CurrentMission = "CurrentMission";
	
	private static String _AWS_DETECTED_ENEMY = "DetectedEnemy";
	
	private static String _CS_MYINFO = "MyInfo";		
	
	private String _AS_ACTION = "Action";
	private enum _AS{ 
		Stop, Fire
	}
	
	
	public DirectEngagement(CEInfo _myInfo) {
		String _name= "DirectEngagementAction";
		SetModelName(_name);
		
		AddInputEvent(_IE_OrderIn);
		AddInputEvent(_IE_MyInfoIn);
		
		AddOutputEvent(_OE_DirectFireOut);
		
		this._id = _myInfo._id;
		
		AddAwState(_AWS_DETECTED_ENEMY, null, true, STATETYPE_OBJECT);
		
		AddConState(_CS_MYINFO, _myInfo, true, STATETYPE_OBJECT);
		
		AddActState(_AS_ACTION, _AS.Stop, true, STATETYPE_CATEGORY);
		
		
	}

	@Override
	public boolean Act(Message msg) {
		if(this.GetActStateValue(_AS_ACTION) == _AS.Stop){
			return true;
		}else if(this.GetActStateValue(_AS_ACTION) == _AS.Fire){
			// TODO msg generating
			CEInfo _enemyInfo = (CEInfo)this.GetAWStateValue(_AWS_DETECTED_ENEMY);
			MsgDirectFire _dirFireMsg = null;
			if(this._id._side == UUID.UUIDSideType.Blue){
				_dirFireMsg = new MsgDirectFire(_enemyInfo._id,this._id, WTType.B_DirectFire);
			}else if(this._id._side == UUID.UUIDSideType.Red){
				_dirFireMsg = new MsgDirectFire(_enemyInfo._id,this._id, WTType.R_DirectFire);
			}
			
			msg.SetValue(_OE_DirectFireOut, _dirFireMsg);
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
			MsgOrder _msgDirectEngOrder = (MsgOrder)this.GetAWStateValue(_AWS_CurrentMission);
			
			if(_msgDirectEngOrder._orderType == OrderType.STOP){
				this.UpdateActStateValue(_AS_ACTION, _AS.Stop);
				this.UpdateAWStateValue(_AWS_DETECTED_ENEMY, null);
			}else if(_msgDirectEngOrder._orderType == OrderType.DirectEngagement){
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
			
			this.UpdateAWStateValue(_AWS_CurrentMission, _orderMsg);
			
			if(_orderMsg._orderType == OrderType.DirectEngagement){
				MsgDirEngOrder _dirOrdMsg = (MsgDirEngOrder)_orderMsg._orderMsg;
				this.UpdateAWStateValue(_AWS_DETECTED_ENEMY, _dirOrdMsg._enemyInfo);
			}
			
			return true;	
		}else if(msg.GetDstEvent() == _IE_MyInfoIn){
			CEInfo _myInfo = (CEInfo)msg.GetValue();
			
			this.UpdateConStateValue(_CS_MYINFO, _myInfo);
			return true;
		}
		
		return false;
	}

	@Override
	public double TimeAdvance() {
		if(this.GetActStateValue(_AS_ACTION) == _AS.Stop){
			return Double.POSITIVE_INFINITY;
		}else if(this.GetActStateValue(_AS_ACTION) == _AS.Fire){
			return 1;
		}
		return 0;
	}

}
