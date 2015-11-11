package zicox.esc;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import zicox.esc.StatusBox;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.Button;

public class Demo_ad_escActivity extends Activity {
    /** Called when the activity is first created. */
	public static BluetoothAdapter myBluetoothAdapter;
	public String SelectedBDAddress;
	StatusBox statusBox;
	public static String ErrorMessage;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        if(!ListBluetoothDevice())finish();
        Button Button1 = (Button) findViewById(R.id.button1);
        statusBox = new StatusBox(this,Button1);
        ErrorMessage = "";
        Button1.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Print1(SelectedBDAddress);
			}
        });
        Button Button2 = (Button) findViewById(R.id.button2);
        Button2.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Print2(SelectedBDAddress);
			}
        });
        Button Button3 = (Button) findViewById(R.id.button3);
        Button3.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Print3(SelectedBDAddress);
			}
        });
        Button Button4 = (Button) findViewById(R.id.button4);
        Button4.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Print4(SelectedBDAddress);
			}
        });
        Button Button5 = (Button) findViewById(R.id.button5);
        Button5.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Print5(SelectedBDAddress);
			}
        });
        Button Button6 = (Button) findViewById(R.id.button6);
        Button6.setOnClickListener(new Button.OnClickListener()
        {
			public void onClick(View arg0)
			{
				Print6(SelectedBDAddress);
			}
        });
    }
    public boolean ListBluetoothDevice()
    {
        final List<Map<String,String>> list=new ArrayList<Map<String, String>>(); 
        ListView listView = (ListView) findViewById(R.id.listView1);
        SimpleAdapter m_adapter = new SimpleAdapter( this,list,
		   		android.R.layout.simple_list_item_2,
		   		new String[]{"DeviceName","BDAddress"},
		   		new int[]{android.R.id.text1,android.R.id.text2}
		   		);
        listView.setAdapter(m_adapter);

        if((myBluetoothAdapter = BluetoothAdapter.getDefaultAdapter())==null)
        {
     		Toast.makeText(this,"没有找到蓝牙适配器", Toast.LENGTH_LONG).show();
     		return false;
        }

        if(!myBluetoothAdapter.isEnabled())
        {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);    
            startActivityForResult(enableBtIntent, 2);
        }

        Set <BluetoothDevice> pairedDevices = myBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() <= 0)return false;
        for (BluetoothDevice device : pairedDevices)
        {
        	Map<String,String> map=new HashMap<String, String>();
        	map.put("DeviceName", device.getName()); 
        	map.put("BDAddress", device.getAddress());
        	list.add(map);
        }
        listView.setOnItemClickListener(new ListView.OnItemClickListener() 
        {
        	public void onItemClick(AdapterView<?> parent, View view, int position, long id) 
        	{
        		SelectedBDAddress = list.get(position).get("BDAddress");
        		if (((ListView)parent).getTag() != null){
        			((View)((ListView)parent).getTag()).setBackgroundDrawable(null);
        		}
        		((ListView)parent).setTag(view);
        		view.setBackgroundColor(Color.BLUE);
			}
        });
        return true;
    }
    public static int zp_realtime_status(int  timeout) 
	{
		byte data[] = new byte[10];
		data[0]=0x1f;
		data[1]=0x00;
		data[2]=0x06;
		data[3]=0x00;
		data[4]=0x07;
		data[5]=0x14;
		data[6]=0x18;
		data[7]=0x23;
		data[8]=0x25;
		data[9]=0x32;
    	BtSPP.SPPWrite(data,10);
		byte readata[] = new byte[1];
		if(!BtSPP.SPPReadTimeout(readata,1,timeout))
		{
			return -1;
		}
		int status= readata[0];
		if((status&1)!=0)ErrorMessage = "打印机纸仓盖开";
		if((status&2)!=0)ErrorMessage = "打印机缺纸";
		if((status&4)!=0)ErrorMessage = "打印头过热";
    	return status;
	}
	
	
	public void showMessage(String str)
	{
		Toast.makeText(this,str, Toast.LENGTH_LONG).show();
	}
	public void Print1(String BDAddress) 
	{
		statusBox.Show("正在打印...");
		if(!BtSPP.OpenPrinter(BDAddress))
		{
			Toast.makeText(this,BtSPP.ErrorMessage, Toast.LENGTH_LONG).show();
			statusBox.Close();
			return;
		}
		try {
		BtSPP.SPPWrite(new byte[]{0x1B,0x40});		//打印机复位
		BtSPP.SPPWrite(new byte[]{0x1B,0x33,0x00});	//设置行间距为0
		BtSPP.SPPWrite("\n".getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1B,0x61,0x00});	//设置不居中
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x01});	//设置倍高
		BtSPP.SPPWrite(String.format("    %-16s", "苏驰物流").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x48,0x02});	//设置条码内容打印在条码下方
		BtSPP.SPPWrite(new byte[]{0x1d,0x77,0x03});	//设置条码宽度0.375
		BtSPP.SPPWrite(new byte[]{0x1d,0x68,0x40});	//设置条码高度64
		//打印code128条码
		BtSPP.SPPWrite(new byte[]{0x1D,0x6B,0x08});
		BtSPP.SPPWrite("1234567890\0".getBytes("GBK"));
		BtSPP.SPPWrite("\n".getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x00});	//设置不倍高
		BtSPP.SPPWrite(String.format("┏━━┳━━━━━━━┳━━┳━━━━━━━━┓\n").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x01});	//设置倍高
		BtSPP.SPPWrite(String.format("┃发站┃%-12s┃到站┃%-14s┃\n","杭州","宝应").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x00});	//设置不倍高
		BtSPP.SPPWrite(String.format("┣━━╋━━━━━━━╋━━╋━━━━━━━━┫\n").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x01});	//设置倍高
		BtSPP.SPPWrite(String.format("┃件数┃%6d/%-7d┃单号┃%-16d┃\n",1,222,55555555).getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x00});	//设置不倍高
		BtSPP.SPPWrite(String.format("┣━━┻┳━━━━━━┻━━┻━━━━━━━━┫\n").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x01});	//设置倍高
		BtSPP.SPPWrite(String.format("┃收件人┃%-28s┃\n","【送】孙俊/宁新富").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x00});	//设置不倍高
		BtSPP.SPPWrite(String.format("┣━━━╋━━━━━━┳━━┳━━━━━━━━┫\n").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x01});	//设置倍高
		BtSPP.SPPWrite(String.format("┃业务员┃%-10s┃名称┃%-14s┃\n","测试","杭州").getBytes("GBK"));
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x00});	//设置不倍高
		BtSPP.SPPWrite(String.format("┗━━━┻━━━━━━┻━━┻━━━━━━━━┛\n").getBytes("GBK"));
		
		BtSPP.SPPWrite(new byte[]{0x1b,0x61,0x01});	//设置居中
		BtSPP.SPPWrite(new byte[]{0x1d,0x21,0x01});	//设置倍高
		BtSPP.SPPWrite(String.format("日期：%10s\n","2011-05-16").getBytes("GBK"));

		BtSPP.SPPWrite("\n\n\n\n".getBytes("GBK"));
		if(zp_realtime_status(5000)>0)
		{
			showMessage(ErrorMessage);
		}
		statusBox.Close();
			
		} catch (UnsupportedEncodingException e) {
		}

		BtSPP.SPPClose();
	}
	
	public void Print2(String BDAddress) 
	{
		statusBox.Show("Cancel...");
		if(!BtSPP.OpenPrinter(BDAddress))
		{
			Toast.makeText(this,BtSPP.ErrorMessage, Toast.LENGTH_LONG).show();
			statusBox.Close();
			return;
		}
		try {

	
		BtSPP.SPPWrite(String.format("\n").getBytes("GBK"));
		
		if(zp_realtime_status(8000)>0)
			showMessage(ErrorMessage);
		
		statusBox.Close();
		} catch (UnsupportedEncodingException e) {
		}
		BtSPP.SPPClose();
	}
	public void Print3(String BDAddress) 
	{
		statusBox.Show("正在打印...");
		if(!BtSPP.OpenPrinter(BDAddress))
		{
			Toast.makeText(this,BtSPP.ErrorMessage, Toast.LENGTH_LONG).show();
			statusBox.Close();
			return;
		}
		try {
		
			
			//BtSPP.SPPWrite(new byte[]{0x1B,0x40});		//打印机复位
			//BtSPP.SPPWrite(new byte[]{0x1B,0x61,0x01});	//居中	
			BtSPP.SPPWrite("===================================== \n".getBytes("GBK"));	
			BtSPP.SPPWrite("\n \n".getBytes("GBK"));	
			BtSPP.SPPWrite("******** XXXXXXXXX ******** \n".getBytes("GBK"));
			BtSPP.SPPWrite("\n \n".getBytes("GBK"));
			
			BtSPP.SPPWrite("===================================== \n".getBytes("GBK"));	
			BtSPP.SPPWrite("\n \n".getBytes("GBK"));	
			BtSPP.SPPWrite("******** XXXXXXXXX ******** \n".getBytes("GBK"));
			BtSPP.SPPWrite("\n \n".getBytes("GBK"));
			
			if(zp_realtime_status(8000)>0)
				showMessage(ErrorMessage);
			statusBox.Close();
		} catch (UnsupportedEncodingException e) {
		}
		BtSPP.SPPClose();
	}
	public void Print4(String BDAddress) 
	{
		if(!BtSPP.OpenPrinter(BDAddress))
		{
			Toast.makeText(this,BtSPP.ErrorMessage, Toast.LENGTH_LONG).show();
			return;
		}
		BtSPP.SPPWrite(new byte[]{0x0C});		//走到左黑标
		BtSPP.SPPClose();
	}
	public void Print5(String BDAddress) 
	{
		if(!BtSPP.OpenPrinter(BDAddress))
		{
			Toast.makeText(this,BtSPP.ErrorMessage, Toast.LENGTH_LONG).show();
			return;
		}
		BtSPP.SPPWrite(new byte[]{0x0E});		//走到右黑标
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		BtSPP.SPPClose();
	}
	public void Print6(String BDAddress) 
	{
		if(!BtSPP.OpenPrinter(BDAddress))
		{
			Toast.makeText(this,BtSPP.ErrorMessage, Toast.LENGTH_LONG).show();
			return;
		}
		BtSPP.SPPWrite(new byte[]{0x1D,(byte) 0x0C});		//走到标签处
		try {Thread.sleep(1000);} catch (InterruptedException e) {}
		BtSPP.SPPClose();
	}
}