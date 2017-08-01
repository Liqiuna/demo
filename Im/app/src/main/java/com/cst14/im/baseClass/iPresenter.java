package com.cst14.im.baseClass;

import com.cst14.im.protobuf.ProtoClass;

public interface iPresenter {
	//运行在子线程
	public void onProcess(ProtoClass.Msg msg);
}
