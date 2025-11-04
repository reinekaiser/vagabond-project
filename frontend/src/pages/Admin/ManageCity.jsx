import React, { useState, useEffect } from 'react';
import { IoSearch } from "react-icons/io5";
import { IoMdAddCircleOutline, IoMdRefresh } from "react-icons/io";
import { FaPlus } from "react-icons/fa6";
import { MdEdit, MdDelete, MdAdd } from "react-icons/md";
import { useNavigate } from "react-router-dom";
import { useGetCitiesQuery } from '../../redux/api/cityApiSlice';
import { Dialog, DialogActions, DialogContent, DialogContentText, DialogTitle, Button, TextField, IconButton, Divider } from '@mui/material';
import { toast } from 'react-toastify';
import CityCard from './CityCard';
import { Delete as DeleteIcon, Add as AddIcon } from '@mui/icons-material';

const ManageCity = () => {
    const navigate = useNavigate();
    const [search, setSearch] = useState('');
    const [searched, setSearched] = useState(false);
    const [filteredResults, setFilteredResults] = useState([]);
    const { data: citiesResponse, isLoading, error, refetch } = useGetCitiesQuery();


    // Xử lý tìm kiếm
    useEffect(() => {
        if (!search.trim() || !citiesResponse) {
            setFilteredResults([]);
            setSearched(false);
            return;
        }

        const searchTerm = search.trim().toLowerCase();
        const cities = Array.isArray(citiesResponse.data) ? citiesResponse.data : 
                      Array.isArray(citiesResponse) ? citiesResponse : [];
        
        const results = cities.filter(city => {
            const cityName = city?.name?.toLowerCase() || '';
            return cityName.includes(searchTerm);
        });

        setFilteredResults(results);
        setSearched(true);
    }, [search, citiesResponse]);

    const handleSearch = (e) => {
        e?.preventDefault();
        if (!search.trim()) {
            setSearched(false);
            setFilteredResults([]);
        } else {
            setSearched(true);
        }
    };


    if (isLoading) {
        return (
            <div className='bg-softBlue min-h-screen p-4 md:p-8'>
                <div className='bg-white rounded-lg shadow-md mt-4 p-4 md:p-6 flex justify-center items-center'>
                    <p className='text-gray-500'>Đang tải dữ liệu...</p>
                </div>
            </div>
        );
    }

    if (error) {
        return (
            <div className='bg-softBlue min-h-screen p-4 md:p-8'>
                <div className='bg-white rounded-lg shadow-md mt-4 p-4 md:p-6'>
                    <p className='text-red-500'>Có lỗi khi tải dữ liệu: {error.message}</p>
                    <button 
                        onClick={refetch}
                        className='mt-4 px-4 py-2 bg-blue-500 text-white rounded hover:bg-blue-600'
                    >
                        Thử lại
                    </button>
                </div>
            </div>
        );
    }

    const cities = Array.isArray(citiesResponse?.data) ? citiesResponse.data : 
                  Array.isArray(citiesResponse) ? citiesResponse : [];
    const validCities = cities.filter(city => city && typeof city.name === 'string');

    return (
        <div className='bg-softBlue min-h-screen p-4 md:p-8'>
            <div className='mx-auto'>
                <div className='flex items-center'>
                    <p className='flex-1 font-semibold text-[20px] md:text-[24px]'>Tất cả thành phố</p>
                    <button
                        className='py-2 px-4 rounded-lg bg-blue-500 text-white font-semibold text-[14px] flex items-center'
                        onClick={() => navigate('create-city')}
                    >
                        <FaPlus className='mr-2' />
                        Thêm thành phố
                    </button>
                </div>
                <div className='bg-white rounded-lg shadow-md mt-4 p-4 md:p-6'>
                    {/* Search Bar */}
                    <form onSubmit={handleSearch} className='flex flex-col sm:flex-row sm:items-center gap-3'>
                        <span className='text-[16px] font-medium text-gray-500'>Tìm kiếm</span>
                        <div className='flex items-center border border-gray-300 rounded-lg p-2 focus-within:border-gray-600'>
                            <input
                                type='text'
                                placeholder='Tìm kiếm thành phố...'
                                className='text-[14px] outline-none flex-1'
                                value={search}
                                onChange={(e) => {
                                    setSearch(e.target.value);
                                    if (!e.target.value.trim()) {
                                        setSearched(false);
                                        setFilteredResults([]);
                                    }
                                }}
                            />
                            <button type="submit" className='p-1 hover:bg-gray-100 rounded-full'>
                                <IoSearch className='text-gray-500 text-[20px]' />
                            </button>
                        </div>

                        <div className='ml-auto flex items-center'>
                            <button 
                                type="button"
                                className='hover:bg-gray-100 p-2 rounded-full' 
                                onClick={refetch}
                            >
                                <IoMdRefresh className='text-[28px] text-gray-400' />
                            </button>
                        </div>
                    </form>

                    {/* Results */}
                    <div className='mt-4 p-4'>
                        <div className='grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4'>
                            {(searched ? filteredResults : validCities).map((city) => (
                                <CityCard
                                    key={city._id}
                                    city={city}
                                />
                            ))}
                        </div>
                    </div>
                </div>
            </div>


        </div>
    );
};

export default ManageCity; 