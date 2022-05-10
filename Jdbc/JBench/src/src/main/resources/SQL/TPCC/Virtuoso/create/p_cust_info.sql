--  
--  This file is part of the OpenLink Software Virtuoso Open-Source (VOS)
--  project.
--  
--  Copyright (C) 1998-2021 OpenLink Software
--  
--  This project is free software; you can redistribute it and/or modify it
--  under the terms of the GNU General Public License as published by the
--  Free Software Foundation; only version 2 of the License, dated June 1991.
--  
--  This program is distributed in the hope that it will be useful, but
--  WITHOUT ANY WARRANTY; without even the implied warranty of
--  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
--  General Public License for more details.
--  
--  You should have received a copy of the GNU General Public License along
--  with this program; if not, write to the Free Software Foundation, Inc.,
--  51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA
--  
--  
create procedure cust_info (
    in w_id integer,
    in d_id integer,
    inout _c_id integer,
    inout _c_last varchar,
    out _c_discount float,
    out _c_credit varchar)
{
  whenever not found goto err; 
  select c_last, c_discount, c_credit into _c_last, _c_discount, _c_credit
    from tpcc..customer
    where c_w_id = w_id
      and c_d_id = d_id
      and c_id = _c_id; 
  return; 

err:
  signal ('BOCUS', 'No customer'); 
}
