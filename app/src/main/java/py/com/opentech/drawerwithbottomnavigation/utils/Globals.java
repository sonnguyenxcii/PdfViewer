package py.com.opentech.drawerwithbottomnavigation.utils;

import androidx.lifecycle.MutableLiveData;

import java.util.List;

import py.com.opentech.drawerwithbottomnavigation.model.PdfModel;
import py.com.opentech.drawerwithbottomnavigation.model.SortModel;

public class Globals {

    private MutableLiveData<List<PdfModel>> listData = new MutableLiveData<>();
    public MutableLiveData<SortModel> sortData = new MutableLiveData<>();
    public MutableLiveData<Boolean> isListMode = new MutableLiveData<>();

    public MutableLiveData<List<PdfModel>> getListData() {
        return listData;
    }

    public void setListData(MutableLiveData<List<PdfModel>> listData) {
        this.listData = listData;
    }
}
