import {useState} from 'react'
import {MdEdit, MdDelete, MdMoreVert, MdVisibility, MdDeleteForever} from "react-icons/md";
import { LuMapPin } from "react-icons/lu";
import { FaRegCalendarAlt } from "react-icons/fa";
import {CLOUDINARY_BASE_URL} from "../../constants/hotel.js";
import {Dropdown, Modal} from "antd";
import {BsThreeDotsVertical} from "react-icons/bs";
import {TbEyeEdit} from "react-icons/tb";
import {useDeleteCityMutation} from "../../redux/api/cityApiSlice.js";
import CityEditModal from "../../components/CityEditModal.jsx";

const CityCard = ({ city }) => {

    const images = city.images?.map(img => {
        return img ? `${CLOUDINARY_BASE_URL}/${img}` : null
    })
    const menuActions = [
        {
            key: "1",
            label: (
                <div
                    className="flex items-center w-[130px]"
                    onClick={() =>
                        setIsUpdateModalOpen(true)
                    }
                >
                    <TbEyeEdit className="text-[20px] mr-2" />
                    Xem và sửa
                </div>
            ),
        },
        {
            key: "2",
            label: (
                <div
                    className="flex items-center w-[100px] text-red-500"
                    onClick={() => setIsDeleteModalOpen(true)}
                >
                    <MdDeleteForever className="text-[20px] mr-2" />
                    Xoá
                </div>
            ),
        },
    ];

    const [deleteCity, {isLoading: isDeletingCity}] = useDeleteCityMutation()
    const handleDeleteTour = async () => {
        try {
            await deleteCity(city._id).unwrap();
            setIsDeleteModalOpen(false);
        } catch (error) {
            console.error("Lỗi khi xoá tour:", error);
        }
    };

  const mainImg = images[0] || null;
  const subImgs = images.slice(1, 3);
  const remainCount = images.length - 3;

    const [isUpdateModalOpen, setIsUpdateModalOpen] = useState(false)
    const [isDeleteModalOpen, setIsDeleteModalOpen] = useState(false)

  return (
    <div 
      className='p-2 rounded-xl shadow-md border border-gray-100 h-[340px] relative hover:shadow-xl duration-300 bg-white flex flex-col'

    >
      <div className="w-full h-[160px] grid grid-cols-3 gap-1 rounded-xl overflow-hidden mb-2">
        <div className="col-span-2 h-full">
          {mainImg ? (
            <img src={mainImg} alt={city.name} className="w-full h-full object-cover rounded-tl-xl rounded-bl-xl" />
          ) : (
            <div className="w-full h-full bg-gray-200 flex items-center justify-center text-gray-400">Không có hình ảnh</div>
          )}
        </div>
        <div className="flex flex-col h-full gap-1">
          {subImgs.map((img, idx) => (
            <img key={idx} src={img} alt={city.name + idx} className={`w-full h-1/2 object-cover ${idx === 0 ? 'rounded-tr-xl' : ''}`} />
          ))}
          {remainCount > 0 && (
            <div className="w-full h-1/2 bg-black bg-opacity-60 text-white flex items-center justify-center rounded-br-xl text-[16px] font-semibold">
              +{remainCount}
            </div>
          )}
        </div>
      </div>
      <div className="flex-1 flex flex-col justify-between px-1 pt-1 pb-2">
        <div>
          <div className="flex items-center gap-2 mb-1 text-gray-400 text-[13px]">
            <FaRegCalendarAlt className="text-[15px]" />
            Ngày thêm: {city.createdAt ? new Date(city.createdAt).toLocaleDateString('vi-VN') : '---'}
          </div>
          <h3 className='text-[18px] font-bold hover:underline duration-300 pb-1 w-fit text-black'>
            {city.name}
          </h3>

          <p className='text-[14px] text-gray-600 line-clamp-2'>{city.description}</p>
        </div>
        {city.popularPlaces && city.popularPlaces.length > 0 && (
          <div className="mt-1">
            <span className="font-medium text-[13px]">Địa điểm nổi bật:</span>
            <ul className="pl-4 text-[13px] text-gray-500 flex gap-2">
              {city.popularPlaces.slice(0, 2).map((place, idx) => (
                <li key={idx}>{place.name}</li>
              ))}
              {city.popularPlaces.length > 2 && (
                <li>+{city.popularPlaces.length - 2} địa điểm khác</li>
              )}
            </ul>
          </div>
        )}
      </div>
        <Dropdown
            menu={{ items: menuActions }}
            placement="topRight"
            arrow
            trigger={["click"]}
        >
            <div className="absolute bottom-3 right-[8px] p-2 rounded-full hover:bg-gray-100 cursor-pointer">
                <BsThreeDotsVertical className="text-gray-400 w-4 h-4"></BsThreeDotsVertical>
            </div>
        </Dropdown>
        <Modal
            title="Xác nhận xóa tour"
            open={isDeleteModalOpen}
            onOk={handleDeleteTour}
            onCancel={() => setIsDeleteModalOpen(false)}
            okText="Xóa"
            cancelText="Hủy"
            okButtonProps={{ danger: true, loading: isDeletingCity }}
        >
            <p>Bạn có chắc chắn muốn xoá thành phố này không?</p>
        </Modal>
        <CityEditModal
            city={city}
            open={isUpdateModalOpen} onCancel={() => setIsUpdateModalOpen(false) }></CityEditModal>
    </div>
  );
};

export default CityCard; 