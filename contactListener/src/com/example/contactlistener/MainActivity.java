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
		// TODO �Զ����ɵķ������
    	//ע��һ���������ݿ�ļ�����
    	this.getContentResolver().registerContentObserver(RawContacts.CONTENT_URI, true, contactobserver);
		
	}
    //����տ�ʼʱ���У�����sd.xml����ʹ��
    private void queryIdAndVersion()
    {
    	String id="";
    	String version="";
    	ContentResolver resolver=this.getContentResolver();
    	Cursor phoneCursor=resolver.query(RawContacts.CONTENT_URI, PHONES_PROJECTION, RawContacts.DELETED+"==0 and 1=="+RawContacts.DIRTY, null, null);
    	if(phoneCursor!=null)                             //�˴�ȡ�ж�dirtyΪ1��ԭ�����ҷ����ҵ�ͨѶ¼��db�ᱻ�ֻ�QQ�ı䣬�ֻ�qq���dirty���0.������׿ͨѶ¼���ݵ�ɾ��ֻ�ǰ�deleted��Ϊ1
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
    //��һ���ж��Ƿ��޸�ͨѶ¼��ע�⣺��绰ʱ�ᴥ�����˷�������Ϊ������URi�Ĺ�ϵ
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
    			//�ҵ��ˣ����ǰ汾��һ����˵�������˴���ϵ�˵���Ϣ
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
    		//���û�����µ��������ҵ���ϵ��
  		  if(j>=k)
  		  {
  			  deletedContacts.add(mid[i]+"#"+mversion[i]);
  			  Log.v(TAG,mid[i]+ " " +mversion[i]);
  		  }
    	}
    	//���������ӵ���Ա
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
    //֪ͨ����Ϣ
	private void notifyMessage()
    {
		ArrayList<String> phones = new ArrayList<String>();
		
		//��ȡ�����ӵ���ϵ�˵�ȫ������
		for (int i = 0; i < addedContacts.size(); i++) {
			String[] str = addedContacts.get(i).split("#");
			ArrayList<String> p = getNewPhones(str[0]);
			phones.addAll(p);
			
			
		}
		
		//��ȡ�޸ĺ����ϵ�˵�ȫ������
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
    	           .setContentText("�޸�"+changedContacts.size()+"  ɾ��"+deletedContacts.size()+"  ����"+addedContacts.size());
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
			// ��ʽ���ֻ���
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
