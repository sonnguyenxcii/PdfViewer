package py.com.opentech.drawerwithbottomnavigation.api;


import java.util.List;

import io.reactivex.Observable;
import py.com.opentech.drawerwithbottomnavigation.model.BaseResponseModel;
import py.com.opentech.drawerwithbottomnavigation.model.BookModel;
import py.com.opentech.drawerwithbottomnavigation.model.ResultModel;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ConvertApiService {

    @GET("book")
    Observable<BookModel> getBookDetail(@Query("gutenberg_id") String gutenberg_id);
}
