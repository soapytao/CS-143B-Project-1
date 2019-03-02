import java.io.*;
import java.util.*;
import java.lang.*;

class PCB
{
  String pid;
	String status;
	int priority;
	int resources[];
	int req_blocked;

	PCB parent;
	LinkedList<PCB> children;

	PCB(String id, PCB par, String s, int p)
	{
		this.pid = id;
		this.status = s;
		this.priority = p;
		resources = new int[4];
		for (int i = 0; i < 4; ++i)
		{
			resources[i] = 0;
		}
		this.req_blocked = 0;

		this.parent = par;
		this.children = new LinkedList<PCB>();
	}
}
