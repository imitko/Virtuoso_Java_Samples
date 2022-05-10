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
create procedure c_by_name (
    in w_id integer,
    in d_id integer,
    in name varchar,
    out id integer)
{
  declare n, c_count integer; 
  declare c_cur cursor for
    select c_id
      from tpcc..customer
      where c_w_id = w_id
       and c_d_id = d_id
       and c_last = name
      order by c_w_id, c_d_id, c_last, c_first; 

  select count (*) into c_count
    from tpcc..customer
    where c_w_id = w_id
      and c_d_id = d_id
      and c_last = name; 

  n := 0; 
  open c_cur; 
  whenever not found goto notfound; 
  while (n <= c_count / 2)
    {
      fetch c_cur into id; 
      n := n + 1; 
    }
  return; 

notfound:
  signal ('cnf', 'customer not found by name'); 
  return; 
}
