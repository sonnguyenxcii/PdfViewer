package py.com.opentech.drawerwithbottomnavigation.api;



import java.util.List;

import io.reactivex.Observable;
import py.com.opentech.drawerwithbottomnavigation.model.BaseResponseModel;
import py.com.opentech.drawerwithbottomnavigation.model.ResultModel;
import retrofit2.http.GET;

public interface ApiService {

//    @Multipart
//    @POST("CaiDatChamCong/ChamCong")
//    Observable<BaseResponseModel<List<CanBoModel>>> chamCong(@Part MultipartBody.Part file,
//                                                             @Part("Camera") RequestBody camera,
//                                                             @Part("NgayGioChamCong") RequestBody date
//    );
//
//    @GET("HeThongCanBo/GetListPaging")
//    Observable<BaseResponseModel<Integer>> getUserList(@Query("PageSize") String PageSize,
//                                                    @Query("PageNumber") String PageNumber,
//                                                       @Query("Keyword") String Keyword
//    );

//
    @GET("list-cat?skip=10")
    Observable<BaseResponseModel<List<ResultModel>>> getListCat();
}
