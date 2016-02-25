package com.example.contactlistener;
import java.util.ArrayList;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	private String TAG="MainActivity";
	private static final int CONTACT_CHANGE = 1; 
	private contactContentObservers contactobserver;
	ArrayList<String> changedContacts = new ArrayList<String>();
	ArrayList<String> deletedContacts = new ArrayList<String>();
	ArrayList<String> addedContacts = new ArrayList<String>();
	  private static final String[] PHONES_PROJECTION = new String[] {  
	       RawContacts._ID,RawContacts.VERSION };  
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        contactobserver=new contactContentObservers(this,mHandler);
        Long start = System.currentTimeMillis();
        queryIdAndVersion();
        Long end = System.currentTimeMillis();
        Log.i(TAG, "====total time:"+(end-start));
        registerContentObservers();
    }
   
    private void registerContentObservers() {
		// TODO 自动生成的方法存根
    	//注册一个监听数据库的监听器
    	this.getContentResolver().registerContentObserver(RawContacts.CONTENT_URI, true, contactobserver);
		
	}
    //程序刚开始时运行，存入sd.xml后面使用
    private void queryIdAndVersion()
    {
    	String id="";
    	String version="";
    	ContentResolver resolver=this.getContentResolver();
    	Cursor phoneCursor=resolver.query(RawContacts.CONTENT_URI, PHONES_PROJECTION, RawContacts.DELETED+"==0 and 1=="+RawContacts.DIRTY, null, null);
    	if(phoneCursor!=null)                             //此处取判断dirty为1的原因是我发现我的通讯录的db会被手机QQ改变，手机qq会把dirty变成0.。。安卓通讯录数据的删除只是把deleted置为1
    	{
    		while (phoneCursor.moveToNext())
    		{
    			
    			id+=phoneCursor.getString(0)+"#";
    			version+= phoneCursor.getString(1)+"#";
    			
    		    Log.v(TAG,"ID: "+phoneCursor.getString(0));
    			Log.v(TAG,"Version: "+phoneCursor.getString(1));
    		}
    		SharedPreferences sp=this.getSharedPreferences("sd", MODE_PRIVATE);
			Editor editor=sp.edit();
			editor.putString("id",id);
			editor.putString("version",version);
			editor.commit();
    	}
    	phoneCursor.close();
    }
    //进一步判断是否修改通讯录。注意：打电话时会触发到此方法，因为监听的URi的关系
    private void bChange()
    {
    	changedContacts.clear();
    	deletedContacts.clear();
    	addedContacts.clear();
    	String idStr;
    	String versionStr ;
    	ArrayList<String> newid=new ArrayList<String>();
    	ArrayList<String> newversion=new ArrayList<String>();
    	SharedPreferences sp=this.getSharedPreferences("sd", MODE_PRIVATE);
    	idStr=sp.getString("id", "");
    	versionStr=sp.getString("version", "");
    	String []mid=idStr.split("#");
    	String []mversion=versionStr.split("#");
    	ContentResolver resolver=this.getContentResolver();
    	Cursor phoneCursor=resolver.query(RawContacts.CONTENT_URI, PHONES_PROJECTION, RawContacts.DELETED+"==0 and 1=="+RawContacts.DIRTY, null, null);
    	while(phoneCursor.moveToNext())
    	{
    		newid.add(phoneCursor.getString(0));
    		newversion.add(phoneCursor.getString(1));
    	}
    	phoneCursor.close();
    	for(int i=0;i<mid.length;i++)
    	{
    		int k=newid.size();
    		int j;
    		for(j=0;j<k;j++)
    		 {
    			//找到了，但是版本不一样，说明更新了此联系人的信息
    			if(mid[i].equals(newid.get(j)))
    			{
    				if(!(mversion[i].equals(newversion.get(j))))
    				{
    					changedContacts.add(newid.get(j)+"#"+newversion.get(j));
    					newid.remove(j);
    					newversion.remove(j);
    					break;
    					
    				}
    				if(mversion[i].equals(newversion.get(j)))
    				{  				
    					newid.remove(j);
    					newversion.remove(j);
    					break;			
    				}
    			
    			}
    		}
    		//如果没有在新的链表中找到联系人
  		  if(j>=k)
  		  {
  			  deletedContacts.add(mid[i]+"#"+mversion[i]);
  			  Log.v(TAG,mid[i]+ " " +mversion[i]);
  		  }
    	}
    	//查找新增加的人员
		int n=newid.size();
		for(int m=0;m<n;m++)
		{
			addedContacts.add(newid.get(m)+"#"+newversion.get(m));
		}	
		
    	notifyMessage();	
    }
    private Handler mHandler=new Handler()
    {
    	public void handleMessage(Message msg) {
    	switch(msg.what)
    	{
    	case CONTACT_CHANGE:
    		bChange();
    		break;
    		default:
    			break;	
    	}
    	}
    };
    //通知栏消息
	private void notifyMessage()
    {
		ArrayList<String> phones = new ArrayList<String>();
		
		//获取新增加的联系人的全部号码
		for (int i = 0; i < addedContacts.size(); i++) {
			String[] str = addedContacts.get(i).split("#");
			ArrayList<String> p = getNewPhones(str[0]);
			phones.addAll(p);
			
			
		}
		
		//获取修改后的联系人的全部号码
		for (int i = 0; i < changedContacts.size(); i++) {
			String[] str = changedContacts.get(i).split("#");
			ArrayList<String> p = getNewPhones(str[0]);
			phones.addAll(p);

		}
		
		
		Log.i(TAG, "========"+phones.toString());

    	NotificationCompat.Builder mBuilder=
    			new NotificationCompat.Builder(this)
    	           .setSmallIcon(R.drawable.uu)
    	           .setContentTitle("My notification")
    	           .setContentText("修改"+changedContacts.size()+"  删除"+deletedContacts.size()+"  增加"+addedContacts.size());
    	changedContacts.clear();
    	deletedContacts.clear();
    	addedContacts.clear();
    	
    	queryIdAndVersion();
    	
    	Intent resultIntent=new Intent(this,MyClass.class);  	
        TaskStackBuilder stackBuilder=TaskStackBuilder.create(this);
        stackBuilder.addParentStack(MyClass.class);
        stackBuilder.addNextIntent(resultIntent);   
        PendingIntent resultPendingIntent=
        		stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);     
        mBuilder.setContentIntent(resultPendingIntent);    
        NotificationManager mNotificationManager=(NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);   
        mNotificationManager.notify(0, mBuilder.build());	
    }
    private ArrayList<String> getNewPhones(String rowcolum_id) {
    	
    	ArrayList<String> phoneList = new ArrayList<String>();
		ContentResolver cr = getContentResolver();
		Cursor cursor = cr.query(RawContacts.CONTENT_URI, new String[]{RawContacts.CONTACT_ID}, "_id = ?", new String[]{rowcolum_id}, null);
		
		if(cursor==null){
			return null;
		}
		String contactId = null;
		if(cursor.moveToFirst()){
			contactId = cursor.getString(cursor.getColumnIndex(RawContacts.CONTACT_ID));
		}
		
		Cursor phoneCursor = cr.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?", new String[]{contactId}, null);
		if(phoneCursor == null){
			return null;
		}
		
		while (phoneCursor.moveToNext()){
			String phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
			String type = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
			// 格式化手机号
			phoneNumber = phoneNumber.replace("-", "");
			phoneNumber = phoneNumber.replace(" ", "");
			
			phoneList.add(phoneNumber);
		}
		
		return phoneList;
	}

	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }
    
}
