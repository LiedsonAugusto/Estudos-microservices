import { ClipboardClock } from 'lucide-react';
import React from 'react';

const StatisticsCard = ({ title, value }: { title: string; value: number }) => {
    return (
        <div className='flex justify-between gap-10 items-center bg-blue-700 text-white p-5 rounded-lg'>
            <div>
                <span className='text-sm'>
                    {title}
                </span>
                <div className='text-2xl font-bold'>
                    {value}
                </div>
            </div>
            <div className='bg-gray-100 p-2 rounded-full'>
                <ClipboardClock className='text-blue-700'  />
            </div>
        </div>
    );
}

export default StatisticsCard;
