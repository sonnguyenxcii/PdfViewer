package py.com.opentech.drawerwithbottomnavigation.utils;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import py.com.opentech.drawerwithbottomnavigation.model.PdfModel;

public class Globals {

    private MutableLiveData<List<PdfModel>> listData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isListMode = new MutableLiveData<>();

    public MutableLiveData<List<PdfModel>> getListData() {
        return listData;
    }

    public void setListData(MutableLiveData<List<PdfModel>> listData) {
        this.listData = listData;
    }
}
