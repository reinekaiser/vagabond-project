import { CITY_URL } from "../constants";
import { apiSlice } from "./apiSlice";

export const cityApiSlice = apiSlice.injectEndpoints({
    endpoints: (builder) => ({
        getCities: builder.query({
            query: () => ({
                url: `${CITY_URL}/`
            })
        }),
        createCity: builder.mutation({
            query: (formData) => {
                const token = localStorage.getItem('token');
                return {
                    url: CITY_URL,
                    method: 'POST',
                    body: formData,
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                };
            },
            invalidatesTags: ['Cities']
        }),
        updateCity: builder.mutation({
            query: ({ id, data }) => {
                const token = localStorage.getItem('token');
                // Kiểm tra và log dữ liệu
                console.log('Update city data:', { id, data });
                
                return {
                    url: `${CITY_URL}/${id}`,
                    method: 'PUT',
                    body: data,
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                };
            },
            invalidatesTags: ['Cities']
        }),
        deleteCity: builder.mutation({
            query: (id) => {
                const token = localStorage.getItem('token');
                return {
                    url: `${CITY_URL}/${id}`,
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                };
            },
            invalidatesTags: ['Cities']
        }),
        deletePopularPlace: builder.mutation({
            query: ({ cityId, placeId }) => {
                const token = localStorage.getItem('token');
                return {
                    url: `${CITY_URL}/${cityId}/popular-places/${placeId}`,
                    method: 'DELETE',
                    headers: {
                        'Authorization': `Bearer ${token}`
                    }
                };
            },
            invalidatesTags: ['Cities']
        })
    }),
});

export const {
    useGetCitiesQuery,
    useCreateCityMutation,
    useUpdateCityMutation,
    useDeleteCityMutation,
    useDeletePopularPlaceMutation
} = cityApiSlice;