package zicox.esc;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;

public class BtSPP {
	public static String ErrorMessage="No Error";
	private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private static BluetoothAdapter myBluetoothAdapter;
	private static BluetoothDevice myDevice;
	private static BluetoothSocket mySocket = null;
	private static OutputStream myOutStream = null;
	private static InputStream myInStream = null;
	
	public static boolean OpenPrinter(String BDAddr)
	{
    	if(BDAddr==""||BDAddr==null)
    	{
    		ErrorMessage="没有选择打印机";
    		return false;
    	}
    	myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    	if(myBluetoothAdapter==null)
    	{
    		ErrorMessage="蓝牙系统错误";
    		return false;
    	}
    	myDevice = myBluetoothAdapter.getRemoteDevice(BDAddr);
    	if(myDevice==null)
    	{
    		ErrorMessage="读取蓝牙设备错误";
    		return false;
    	}
		if(!BtSPP.SPPOpen(myBluetoothAdapter, myDevice))
		{
			return false;
		}
    	return true;
	}
	public static boolean SPPOpen(BluetoothAdapter bluetoothAdapter, BluetoothDevice btDevice)
	{
		boolean error=false;
		myBluetoothAdapter = bluetoothAdapter;
		myDevice = btDevice;
		BluetoothSocket tmp=null;

		if(!myBluetoothAdapter.isEnabled())
		{
			ErrorMessage = "蓝牙适配器没有打开";
	        return false;
		}

			try 
			{
	            Method m = myDevice.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
	            try
	            {
	            	tmp = (BluetoothSocket)m.invoke(myDevice, Integer.valueOf(1));
	            }catch(Exception e)
	            {
	            	
	            }
	        }catch(NoSuchMethodException e)
	        {
	        }

	        mySocket = tmp;

/*		
		try 
		{
			mySocket = myDevice.createInsecureRfcommSocketToServiceRecord(SPP_UUID);//.createRfcommSocketToServiceRecord(SPP_UUID);
		}catch(IOException e)
		{
		}
*/		
		myBluetoothAdapter.cancelDiscovery();

        try 
		{
			mySocket.connect();
		} 
		catch (IOException e2) 
		{
			ErrorMessage = e2.getLocalizedMessage();//"无法连接蓝牙打印机";
			mySocket = null;
			return false;
		}

		try 
		{
			myOutStream = mySocket.getOutputStream();
		} 
		catch (IOException e3) 
		{
			myOutStream = null;
			error = true;
		}

		try 
		{
			myInStream = mySocket.getInputStream();
		} 
		catch (IOException e3) 
		{
			myInStream = null;
			error = true;
		}

		if(error)
		{
			SPPClose();
			return false;
		}
		
		return true;
	}
	public static boolean SPPClose()
	{
//		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		if(myOutStream!=null)
		{
			try{myOutStream.flush();}catch (IOException e1){}
			try{myOutStream.close();}catch (IOException e){}
			myOutStream=null;
		}
		if(myInStream!=null)
		{
			try{myInStream.close();}catch(IOException e){}
			myInStream=null;
		}
		if(mySocket!=null)
		{
			//try {mySocket.wait(500);} catch (InterruptedException e1) {}
			try{mySocket.close();}catch (IOException e){}
			mySocket=null;
		}
//		try {Thread.sleep(4000);} catch (InterruptedException e) {}
		return true;
	}
	
	public static boolean SPPWrite(byte[] Data)
	{
		return SPPWrite(Data,Data.length);
/*		
		try 
		{
			myOutStream.write(Data);
		} 
		catch (IOException e) 
		{
			ErrorMessage = "发送蓝牙数据失败";
			return false;
		}
		return true;
*/		
	}
	public static boolean SPPWrite(byte[] Data,int DataLen)
	{
		try 
		{
			myOutStream.write(Data,0,DataLen);
			int delay=DataLen/5;
			if(delay==0)delay=1;
			try{Thread.sleep(delay);}catch(InterruptedException e){}
		} 
		catch (IOException e) 
		{
			ErrorMessage = "发送蓝牙数据失败";
			return false;
		}
		return true;
	}
	public static void SPPFlush()
	{
		int i=0,DataLen=0;
		try 
		{
			DataLen = myInStream.available();
		} 
		catch (IOException e1) 
		{
		}
		for(i=0;i<DataLen;i++)
		{
			try 
			{
				myInStream.read();
			} 
			catch (IOException e) 
			{
			}
		}
	}
	public static int SPPReadData(byte[] Data,int DataLen)
	{
		int n=0;
		byte b[]=new byte[8];
		while(SPPReadTimeout(b,1,500))
		{
			Data[n]=b[0];
			n++;
			if(n>=DataLen)break;
		}
		return n;
	}
	public static boolean SPPRead(byte[] Data,int DataLen)
	{
		return SPPReadTimeout(Data,DataLen,2000);
	}
	public static boolean SPPReadTimeout(byte[] Data,int DataLen,int Timeout)
	{
		int i;
		for(i=0;i<(Timeout/50);i++)
		{
			try 
			{
				if(myInStream.available()>=DataLen)
				{
					try 
					{
						myInStream.read(Data,0,DataLen);
						return true;
					} 
					catch (IOException e) 
					{
						ErrorMessage = "读取蓝牙数据失败";
						return false;
					}
				}
			} 
			catch (IOException e) 
			{
				ErrorMessage = "读取蓝牙数据失败";
				return false;
			}
			try 
			{
				Thread.sleep(50);
			} 
			catch (InterruptedException e) 
			{
				ErrorMessage = "读取蓝牙数据失败";
				return false;
			}
		}
		ErrorMessage = "蓝牙读数据超时";
		return false;
	}
}
