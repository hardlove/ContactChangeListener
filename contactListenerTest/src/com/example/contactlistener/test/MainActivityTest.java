package com.example.contactlistener.test;

import com.example.contactlistener.MainActivity;

import android.test.ActivityInstrumentationTestCase2;


/**
 * 
 * @author Administrator
 * 编写测试用力的方法：
 * 
 * eclipse中，右击需要测试的工程，选择Android Tool-->New Test Project...
 * 
 * 1.测试Activity的方法
 * 
 * 1.1继承ActivityInstrumentationTestCase2<T extends Activity>,泛型为需要测试的Activity类名
 * 1.2添加无参构造方法，并在构造方法中调用super(T.class),T为要测试的Activity
 * 1.3增加一个测试前提,按照惯例，验证测试数据集的方法被称为testPreconditions()
 * 1.4选择Run As > Android Junit Test。
 *
 */

public class MainActivityTest extends ActivityInstrumentationTestCase2<MainActivity> {

	/**
	 * 构造函数是由测试用的Runner调用，用于初始化测试类的
	 * @param activityClass
	 */
	


	public MainActivityTest() {
		super(MainActivity.class);
		// TODO Auto-generated constructor stub
	}

	/**
	 * setUp()方法是由测试Runner在其他测试方法开始前运行的
	 * 
	 * 通常在setUp()方法中，我们应该:
	 * 
	 * •为setUp() 调用父类构造函数，这是JUnit要求的。 
	 * •初始化测试数据集的状态，具体而言： 
	 * 	◦定义保存测试数据及状态的实例变量
	 * 	◦创建并保存正在测试的Activity的引用实例。 
	 * 	◦获得想要测试的Activity中任何UI组件的引用。
	 * 我们可以使用getActivity()方法得到正在测试的Activity的引用。
	 * 
	 * 
	 */
	@Override
	protected void setUp() throws Exception {
		// TODO Auto-generated method stub
		super.setUp();
		
		mMainActivity = getActivity();
		
	}  
	
	
	 private MainActivity mMainActivity;

	/**
	 * 我们最好在执行测试之前，检查测试数据集的设置是否正确，以及我们想要测试的对象是否已经正确地初始化。
	 * 这样，测试就不会因为有测试数据集的设置错误而失败 。按照惯例，验证测试数据集的方法被称为testPreconditions()。
	 */
	public void testPreconditions() {
	    assertNotNull("mMainActivity is null", mMainActivity);
	}


	

}
