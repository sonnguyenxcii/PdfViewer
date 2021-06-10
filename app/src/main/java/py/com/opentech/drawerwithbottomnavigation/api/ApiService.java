package py.com.opentech.drawerwithbottomnavigation.api;


import java.util.List;

import io.reactivex.Observable;
import py.com.opentech.drawerwithbottomnavigation.model.BaseResponseModel;
import py.com.opentech.drawerwithbottomnavigation.model.BookModel;
import py.com.opentech.drawerwithbottomnavigation.model.BookRequestModel;
import py.com.opentech.drawerwithbottomnavigation.model.ResultModel;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {


    //
    @GET("list-cat?skip=10")
    Observable<BaseResponseModel<List<ResultModel>>> getListCat();

    @GET("get-cat")
    Observable<BaseResponseModel<List<BookModel>>> getListBookByCat(@Query("subject") String subject);

    @GET("book")
    Observable<BookModel> getBookDetail(@Query("gutenberg_id") String gutenberg_id);

    @POST("epub/convert/url")
    Observable<BookModel> getBookUrl(@Body BookRequestModel body);
}
