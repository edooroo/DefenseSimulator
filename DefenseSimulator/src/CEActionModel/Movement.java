package CEActionModel;

import java.util.ArrayList;

import C2OrderMessage.MsgMoveOrder;
import Common.CEInfo;
import Common.GridInfo;
import Common.GridInfoNetwork;
import Common.XY;
import Message.MsgLocUpdate;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.BasicActionModel;
import edu.kaist.seslab.ldef.engine.modelinterface.internal.Message;

public class Movement extends BasicActionModel {
	public static String _IE_OrderIn = "OrderIn";
	public static String _IE_MyInfo = "MyInfoIn";
	public static String _OE_LocUpdateOut = "LocUpdateOut";
	
	private static String _CS_CurrentLoc = "CurrentLocation"; // XY
	private static String _CS_Movable = "Movable"; // boolean
	private static String _CS_MyInfo = "MyInfo";  // CEInfo
	private static String _CS_CurrentDirection = "CurrentDirection"; // double
	private static String _CS_CurrentObjective = "CurrentObjective"; // XY
	private static String _CS_CurrentSpeed = "CurrentSpeed";
	 
	private static String _AWS_CurrentPath = "CurrentPath"; // ArrayList<XY> 
	
	
	private static String _AS_Action = "Action";
	
	private enum _AS{
		Stop, Move
	}
	
	private double _PARAM_MaxSpeed;

	public Movement(CEInfo _myInfo) {
		String _name = "MovementAction";
		SetModelName(_name);
		
		this._PARAM_MaxSpeed = _myInfo._maxSpeed;
		
		/*
		 * Add Input and Output Port
		 */
		AddInputEvent(_IE_OrderIn);
		AddInputEvent(_IE_MyInfo);
		AddOutputEvent(_OE_LocUpdateOut);
		
		AddConState(_CS_MyInfo, _myInfo);
		AddConState(_CS_CurrentLoc, _myInfo._myLoc);
		AddConState(_CS_Movable, _myInfo._movable);
		
		AddConState(_CS_CurrentDirection, null);
		AddConState(_CS_CurrentObjective, null);
		AddConState(_CS_CurrentSpeed, 0);
		
		AddAwState(_AWS_CurrentPath, null);
		
		AddActState(_AS_Action, _AS.Stop);
	}
	
	private XY UpdateMyLoc(XY currentLoc, double direction, double speed, XY _currCheckpoint) {
		// TODO update new loc
		XY ret = currentLoc.calEndPoint(speed, direction);
		
		return ret;
	}
	
	private boolean isArrive(XY currentLoc, XY dest){
		if(currentLoc.distance(dest)<= 10){
			return true;
		}
		return false;
	}

	@Override
	public boolean Act(Message msg) {
		if(this.GetActStateValue(_AS_Action) == _AS.Stop){
			//  nothing to do
			return true;
		}else if(this.GetActStateValue(_AS_Action) == _AS.Move){
			XY _currCheckpoint = (XY)this.GetConStateValue(_CS_CurrentObjective);
			double _currDirection = (double)this.GetConStateValue(_CS_CurrentDirection);
			XY _currLoc = (XY)this.GetConStateValue(_CS_CurrentLoc);
			double _currSpeed = (double)this.GetConStateValue(_CS_CurrentSpeed);
			CEInfo _currInfo = (CEInfo)this.GetConStateValue(_CS_MyInfo);
			
			

			XY _newLoc = this.UpdateMyLoc(_currLoc, _currDirection, _currSpeed, _currCheckpoint);
			this.UpdateConStateValue(_CS_CurrentLoc, _newLoc);
			
			CEInfo _newInfo = new CEInfo(_currInfo);
			_newInfo._myLoc = _newLoc;
			
			if(_currCheckpoint.distance(_newLoc) <= GridInfo._r){
				//TODO make calculating speed in the Grid
				GridInfo _newGridInfo = GridInfoNetwork.findMyGrid(_newLoc);
				_newInfo._currentGrid = _newGridInfo;
				
				_currSpeed = _newGridInfo.getSpeedInThisGrid(this._PARAM_MaxSpeed);
				
				this.UpdateConStateValue(_CS_CurrentSpeed, _currSpeed);
			}
			
			
			
			if(this.isArrive(_newLoc, _currCheckpoint)){
				ArrayList<XY> _pathList = (ArrayList<XY>)this.GetAWStateValue(_AWS_CurrentPath);
				
				if(_pathList.isEmpty()){
					this.UpdateConStateValue(_CS_CurrentDirection, null);
					this.UpdateConStateValue(_CS_CurrentObjective, null);
					this.UpdateAWStateValue(_AWS_CurrentPath, null);
					
				}else {
					_currCheckpoint = _pathList.remove(0);
					_currDirection = _currLoc.calBearing(_currCheckpoint);
					
					this.UpdateConStateValue(_CS_CurrentObjective, _currCheckpoint);
					this.UpdateConStateValue(_CS_CurrentDirection, _currDirection);
					this.UpdateAWStateValue(_AWS_CurrentPath, _pathList);
					
				}
				
			}
			
			this.UpdateConStateValue(_CS_MyInfo, _newInfo);
			MsgLocUpdate _locUpdateMsg = new MsgLocUpdate(_newInfo);
			msg.SetValue(_OE_LocUpdateOut, _locUpdateMsg);
			
			return true;
		}
		return false;
	}

	@Override
	public boolean Decide() {
		if(this.GetActStateValue(_AS_Action) == _AS.Stop){
			this.UpdateActStateValue(_AS_Action, _AS.Move);
			return true;
		}else if(this.GetActStateValue(_AS_Action) == _AS.Move){
			ArrayList<XY> _currentPath = (ArrayList<XY>)this.GetAWStateValue(_AWS_CurrentPath);
			if(_currentPath == null){
				this.UpdateActStateValue(_AS_Action, _AS.Stop);	
			}else {
				this.UpdateActStateValue(_AS_Action, _AS.Move);
			}
			return true;
		}

		return false;
	}

	@Override
	public boolean Perceive(Message msg) {
		if(msg.GetDstEvent() == _IE_MyInfo){
			MsgLocUpdate _myInfoMsg = (MsgLocUpdate)msg.GetValue();
			this.UpdateConStateValue(_CS_MyInfo, _myInfoMsg._myInfo);
		}else if(msg.GetDstEvent() == _IE_OrderIn){
			
			MsgMoveOrder _moveOrderMsg = (MsgMoveOrder)msg.GetValue();
			this.UpdateAWStateValue(_AWS_CurrentPath, _moveOrderMsg.getPath());
		}
		return false;
	}

	@Override
	public double TimeAdvance() {
		if(this.GetActStateValue(_AS_Action) == _AS.Stop){
			return Double.POSITIVE_INFINITY;
		}else if(this.GetActStateValue(_AS_Action) == _AS.Move){
			return 1;
		}
		return 0;
	}

}
