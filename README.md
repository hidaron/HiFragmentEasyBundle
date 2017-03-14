# HiFragmentEasyBundle
一个可使用注解快速构建Bundle和注入Bundel的库（仅限于Fragment）

## 怎么注入Bundle
### 1.使用@ExtraModule标识Fragment

````
@ExtraModule() //标识这是注入和构建Bundle的模块
public class DemoFragment extends Fragment {
}
````

### 2.使用@Extra来标识这是要注入Bundle的属性，有3种方式

````
 	@Extra() //方式一，如果要注入的属性是私有属性的，就必须在这个类内提供 >= protected属性的方法，如setName()
    private String mName;

    @Extra() //方式二， 如果要注入的属性是 >= protected，那么就可以直接注入
    protected int mAge;

    @Extra("xingbie") //方式三，也可以使用自定义名字（默认就是属性的名字，例如mName或者name, 对应到Bundle的key就是name）这种方式来绑定Bundle所对应的key
    protected String mSex;
    
    /**
     * 因为mName是私有属性，如果没有该方法，是没有办法注入的
     *
     * @param name
     */
    protected void setName(String name) {
        this.mName = name;
    }
````

### 3.使用EasyExtraInjector.inject()给Fragment注入Bundle
````
	@Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyExtraInjector.inject(this); //一句代码就可以注入Bundle，省去很多功夫
    }
````


## 怎么快速构建Bundle

1.每一个用@ExtraModule标识的Fragment都会自动生成以模块类名+Exta命名的类，我们可以使用这个类来快速构建Bundle，
而这个Bundle里面key就是用@Extra标识属性的属性名，下面就是自动生成的代码

````
public final class DemoFragmentExtra {
  private Bundle bundle = new Bundle();

  public Bundle get() {
    return bundle;
  }

  public DemoFragmentExtra age(int age) {
    bundle.putInt("age", age);
    return this;
  }

  public DemoFragmentExtra sex(String sex) {
    bundle.putString("xingbie", sex);
    return this;
  }

  public DemoFragmentExtra name(String name) {
    bundle.putString("name", name);
    return this;
  }
}
````
2.构建Bundle，可以通过get()来获取到Bundle

````
 	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDemoFragment = new DemoFragment();
        //DemoFragmentExtra就是自动生成的类，用来构建Bundle用
        Bundle bundle = new DemoFragmentExtra().age(18).name("Lim").sex("男").get();
        FragmentHelper.addFragment(getSupportFragmentManager(), mDemoFragment, R.id.fragment_layout, bundle, false);
    }
````

