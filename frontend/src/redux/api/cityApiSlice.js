import { CITY_URL } from "../constants";
import { apiSlice } from "./apiSlice";

export const cityApiSlice = apiSlice.injectEndpoints({
    endpoints: (builder) => ({
        getCities: builder.query({
            query: () => ({
                url: `${CITY_URL}/`
            }),
            providesTags: (result, error, arg) =>
                result
                    ? [
                        ...result.map(({ _id }) => ({ type: 'Cities', id: _id })),
                        { type: 'Cities', id: 'LIST' },
                    ]
                    : [{ type: 'Cities', id: 'LIST' }],
        }),
        createCity: builder.mutation({
            query: (data) => ({
                url: `${CITY_URL}/`,
                method: "POST",
                body: data
            }),
            invalidatesTags: [{ type: 'Cities', id: 'LIST' }],
        }),
        updateCity: builder.mutation({
            query: ({ cityId, data }) => ({
                url: `${CITY_URL}/${cityId}`,
                method: "PUT",
                body: data
            }),
            invalidatesTags: (result, error, { cityId }) => [
                { type: 'Cities', id: cityId },
                { type: 'Cities', id: 'LIST' },
            ],
        }),
        deleteCity: builder.mutation({
            query: (cityId) => ({
                url: `${CITY_URL}/${cityId}`,
                method: "DELETE",
            }),
            invalidatesTags: (result, error, cityId) => [
                { type: 'Cities', id: cityId },
                { type: 'Cities', id: 'LIST' },
            ],
        }),

    }),
});

export const {
    useGetCitiesQuery,
    useCreateCityMutation,
    useUpdateCityMutation,
    useDeleteCityMutation,
    useDeletePopularPlaceMutation
} = cityApiSlice;