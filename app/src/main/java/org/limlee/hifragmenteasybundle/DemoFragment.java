package org.limlee.hifragmenteasybundle;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import org.limlee.annotation.EasyExtraInjector;
import org.limlee.annotation.Extra;
import org.limlee.annotation.ExtraModule;


@ExtraModule() //step1.标识这是注入Bundle的模块，否则Process就不会去该模块查找注入的属性
public class DemoFragment extends Fragment {

    //step2.要给注入的Bundle绑定属性

    @Extra() //方式一，如果要添加注解的属性是私有属性的，就必须在这个类内提供 > protected属性的方法，如setName()
    private String mName;

    @Extra() //方式二， 如果要注解的属性是 > protected，那么就可以绑定到Bundle
    protected int mAge;

    @Extra("xingbie") //方式三，该属性的名字就是Bundle里的key，例如mSex(或者sex)是默认对应Bundle里面的sex，但可以通过这种方式来绑定Bundle
    protected String mSex;

    private TextView mNameTxView;
    private TextView mAgeTxView;
    private TextView mSexTxView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EasyExtraInjector.inject(this); //step3.一句代码就可以注入Bundle，省去很多功夫
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_demo, null, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mNameTxView = (TextView) view.findViewById(R.id.name);
        mAgeTxView = (TextView) view.findViewById(R.id.age);
        mSexTxView = (TextView) view.findViewById(R.id.sex);

        mNameTxView.setText(mName);
        mAgeTxView.setText(String.valueOf(mAge));
        mSexTxView.setText(mSex);
    }

    /**
     * 因为name是私有属性，如果没有该方法，是不能绑定到要注入的Bundle的
     *
     * @param name
     */
    protected void setName(String name) {
        this.mName = name;
    }

}
