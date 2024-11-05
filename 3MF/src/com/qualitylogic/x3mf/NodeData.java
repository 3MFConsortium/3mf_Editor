package com.qualitylogic.x3mf;

import java.util.ArrayList;
import java.util.List;

public class NodeData {
	List<Integer> curLoc =new ArrayList<Integer>();
	List<Integer> hscrLoc =new ArrayList<Integer>();
	List<Integer> vscrLoc =new ArrayList<Integer>();
	List<String> curNode =new ArrayList<String>();
	List<Boolean> dirNode =new ArrayList<Boolean>();
	public NodeData() {

	}
	public void addNode(String node, int cursor){
		if(curNode == null){
			curLoc.add(cursor);
			hscrLoc.add(0);
			vscrLoc.add(0);
			curNode.add(node);
			dirNode.add(false);
			return;
		} else {
			for (int i = 0; i<curNode.size();i++){
				if(curNode.get(i).contentEquals(node)){
					curLoc.set(i, cursor);
					return;
				}
			}
			curLoc.add(cursor);
			hscrLoc.add(0);
			vscrLoc.add(0);
			curNode.add(node);
			dirNode.add(false);
			return;
		}
	}
	public void addNodeH(String node, int hzScr){
		if(curNode == null){
			curLoc.add(0);
			hscrLoc.add(hzScr);
			vscrLoc.add(0);
			curNode.add(node);
			dirNode.add(false);
			return;
		} else {
			for (int i = 0; i<curNode.size();i++){
				if(curNode.get(i).contentEquals(node)){
					hscrLoc.set(i, hzScr);
					return;
				}
			}
			curLoc.add(0);
			hscrLoc.add(hzScr);
			vscrLoc.add(0);
			curNode.add(node);
			dirNode.add(false);
			return;
		}
	}
	public void addNodeV(String node, int vtScr){
		if(curNode == null){
			curLoc.add(0);
			hscrLoc.add(0);
			vscrLoc.add(vtScr);
			curNode.add(node);
			dirNode.add(false);
			return;
		} else {
			for (int i = 0; i<curNode.size();i++){
				if(curNode.get(i).contentEquals(node)){
					vscrLoc.set(i, vtScr);
					return;
				}
			}
			curLoc.add(0);
			hscrLoc.add(0);
			vscrLoc.add(vtScr);
			curNode.add(node);
			dirNode.add(false);
			return;
		}
	}
	public void addNodeDir(String node, boolean bDir){
		if(curNode == null){
			curLoc.add(0);
			hscrLoc.add(0);
			vscrLoc.add(0);
			curNode.add(node);
			dirNode.add(bDir);
			return;
		} else {
			for (int i = 0; i<curNode.size();i++){
				if(curNode.get(i).contentEquals(node)){
					dirNode.set(i, bDir);
					return;
				}
			}
			curLoc.add(0);
			hscrLoc.add(0);
			vscrLoc.add(0);
			curNode.add(node);
			dirNode.add(bDir);
			return;
		}
	}
	public int getHscroll(String node){
		for (int i = 0; i<curNode.size();i++){
			if(curNode.get(i).contentEquals(node)){
				return hscrLoc.get(i);
			}
		}
		return 0;
	}
	public int getVscroll(String node){
		for (int i = 0; i<curNode.size();i++){
			if(curNode.get(i).contentEquals(node)){
				return vscrLoc.get(i);
			}
		}
		return 0;
	}
	public int getCursor(String node){
		for (int i = 0; i<curNode.size();i++){
			if(curNode.get(i).contentEquals(node)){
				return curLoc.get(i);
			}
		}
		return 0;
	}
	public boolean getDir(String node){
		for (int i = 0; i<curNode.size();i++){
			if(curNode.get(i).contentEquals(node)){
				return dirNode.get(i);
			}
		}
		return false;
	}
		public void clear(){
			curLoc.clear();
			hscrLoc.clear();
			vscrLoc.clear();
			curNode.clear();
			dirNode.clear();
		}

}
