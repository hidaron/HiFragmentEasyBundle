package org.limlee.hifragmenteasybundle;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    private Fragment mDemoFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mDemoFragment = new DemoFragment();
        //每一个要注入Bundel的模块都会自动生成该模块类名+Exta命名的类，例如DemoFragment会生成一个DemoFragmentExtra这样的类
        //这样我们可以通过DemoFragmentExtra来简化我们构建Bundel的过程
        Bundle bundle = new DemoFragmentExtra().age(18).name("Lim").sex("男").get();
        FragmentHelper.addFragment(getSupportFragmentManager(), mDemoFragment, R.id.fragment_layout, bundle, false);
    }
}
